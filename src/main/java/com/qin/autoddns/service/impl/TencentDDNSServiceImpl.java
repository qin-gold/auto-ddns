package com.qin.autoddns.service.impl;

import com.qin.autoddns.config.DDNSConfig;
import com.qin.autoddns.service.DDNSService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 腾讯云DNS服务实现
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
@Service
public class TencentDDNSServiceImpl implements DDNSService {

    private final Logger log = LoggerFactory.getLogger(TencentDDNSServiceImpl.class);
    private final DnspodClient client;
    private final DDNSConfig config;

    public TencentDDNSServiceImpl(DDNSConfig config) {
        this.config = config;
        try {
            // 验证配置
            validateConfig();
            
            // 初始化客户端
            Credential cred = new Credential(config.getAccessKey(), config.getSecretKey());
            this.client = new DnspodClient(cred, "ap-guangzhou");
            
            // 测试连接
            testConnection();
            
            log.info("腾讯云DNS服务初始化成功");
        } catch (Exception e) {
            log.error("腾讯云DNS服务初始化失败: {}", e.getMessage());
            throw new RuntimeException("DNS服务初始化失败", e);
        }
    }

    private void validateConfig() {
        if (config.getAccessKey() == null || config.getAccessKey().trim().isEmpty()) {
            throw new IllegalArgumentException("AccessKey 不能为空");
        }
        if (config.getSecretKey() == null || config.getSecretKey().trim().isEmpty()) {
            throw new IllegalArgumentException("SecretKey 不能为空");
        }
        log.info("AccessKey: {}, SecretKey长度: {}", 
                maskAccessKey(config.getAccessKey()), 
                config.getSecretKey().length());
    }

    private String maskAccessKey(String accessKey) {
        if (accessKey == null || accessKey.length() < 8) {
            return "***";
        }
        return accessKey.substring(0, 4) + "***" + accessKey.substring(accessKey.length() - 4);
    }

    private void testConnection() {
        try {
            DescribeRecordListRequest request = new DescribeRecordListRequest();
            request.setDomain(config.getDomain());
            client.DescribeRecordList(request);
            log.info("DNS服务连接测试成功");
        } catch (TencentCloudSDKException e) {
            log.error("DNS服务连接测试失败: {}", e.getMessage());
            throw new RuntimeException("DNS服务连接测试失败", e);
        }
    }

    @Override
    public boolean updateDNSRecord(String domain, String subDomain, String recordType, String value) {
        try {
            log.info("开始更新DNS记录: domain={}, subDomain={}, type={}, value={}", 
                    domain, subDomain, recordType, value);
            
            String recordId = getRecordId(domain, subDomain);
            if (recordId != null) {
                return updateExistingRecord(domain, subDomain, recordType, value, recordId);
            } else {
                return createNewRecord(domain, subDomain, recordType, value);
            }
        } catch (TencentCloudSDKException e) {
            log.error("更新DNS记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean updateExistingRecord(String domain, String subDomain, String recordType, 
                                       String value, String recordId) throws TencentCloudSDKException {
        log.info("更新已存在的记录: recordId={}", recordId);
        ModifyRecordRequest request = new ModifyRecordRequest();
        request.setDomain(domain);
        request.setRecordId(Long.parseLong(recordId));
        request.setRecordType(recordType);
        request.setRecordLine("默认");
        request.setValue(value);
        request.setSubDomain(subDomain);

        ModifyRecordResponse response = client.ModifyRecord(request);
        log.info("记录更新成功: {}", response);
        return true;
    }

    private boolean createNewRecord(String domain, String subDomain, String recordType, 
                                  String value) throws TencentCloudSDKException {
        log.info("创建新记录");
        CreateRecordRequest request = new CreateRecordRequest();
        request.setDomain(domain);
        request.setRecordType(recordType);
        request.setRecordLine("默认");
        request.setValue(value);
        request.setSubDomain(subDomain);

        CreateRecordResponse response = client.CreateRecord(request);
        log.info("新记录创建成功: {}", response);
        return true;
    }

    @Override
    public String getCurrentRecord(String domain, String subDomain) {
        try {
            log.debug("获取当前记录: domain={}, subDomain={}", domain, subDomain);
            DescribeRecordListRequest request = new DescribeRecordListRequest();
            request.setDomain(domain);

            DescribeRecordListResponse response = client.DescribeRecordList(request);
            for (RecordListItem record : response.getRecordList()) {
                if (record.getName().equals(subDomain)) {
                    log.debug("找到记录: {}", record.getValue());
                    return record.getValue();
                }
            }
            log.debug("未找到记录");
            return null;
        } catch (TencentCloudSDKException e) {
            log.error("获取当前记录失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getRecordId(String domain, String subDomain) {
        try {
            DescribeRecordListRequest request = new DescribeRecordListRequest();
            request.setDomain(domain);

            DescribeRecordListResponse response = client.DescribeRecordList(request);
            for (RecordListItem record : response.getRecordList()) {
                if (record.getName().equals(subDomain)) {
                    return record.getRecordId().toString();
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("获取记录ID失败: {}", e.getMessage(), e);
        }
        return null;
    }
} 