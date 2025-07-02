package com.qin.autoddns.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainsResponse;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.qin.autoddns.config.DDNSConfig;
import com.qin.autoddns.service.DDNSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * 阿里云DNS服务实现
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
@Service
public class AliyunDDNSServiceImpl implements DDNSService {

    private final Logger log = LoggerFactory.getLogger(AliyunDDNSServiceImpl.class);
    private IAcsClient client;
    private final DDNSConfig config;
    private final Environment environment;

    public AliyunDDNSServiceImpl(DDNSConfig config, Environment environment) {
        this.config = config;
        this.environment = environment;

        // 检查是否激活了阿里云配置
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isAliyunProfileActive = activeProfiles.length == 0 ||
                environment.getProperty("ddns.provider", String.class, "").equals("aliyun");

        if (isAliyunProfileActive) {
            try {
                // 验证配置
                validateConfig();

                // 初始化客户端
                DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", 
                    config.getAccessKey(), config.getSecretKey());
                this.client = new DefaultAcsClient(profile);

                // 测试连接
                testConnection();

                log.info("阿里云DNS服务初始化成功");
            } catch (Exception e) {
                log.error("阿里云DNS服务初始化失败: {}", e.getMessage());
                throw new RuntimeException("DNS服务初始化失败", e);
            }
        } else {
            log.info("阿里云DNS服务未激活，跳过初始化");
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
            DescribeDomainsRequest request = new DescribeDomainsRequest();
            request.setPageSize(1);
            client.getAcsResponse(request);
            log.info("DNS服务连接测试成功");
        } catch (ClientException e) {
            log.error("DNS服务连接测试失败: {}", e.getMessage());
            throw new RuntimeException("DNS服务连接测试失败", e);
        }
    }

    @Override
    public boolean updateDNSRecord(String domain, String subDomain, String recordType, String value) {
        if (client == null) {
            log.warn("阿里云DNS服务未初始化，无法更新记录");
            return false;
        }

        try {
            log.info("开始更新DNS记录: domain={}, subDomain={}, type={}, value={}",
                    domain, subDomain, recordType, value);

            String recordId = getRecordId(domain, subDomain, recordType);
            if (recordId != null) {
                return updateExistingRecord(recordId, subDomain, recordType, value);
            } else {
                return createNewRecord(domain, subDomain, recordType, value);
            }
        } catch (ClientException e) {
            log.error("更新DNS记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean updateExistingRecord(String recordId, String subDomain, String recordType, String value) 
            throws ClientException {
        log.info("更新已存在的记录: recordId={}", recordId);
        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();
        request.setRecordId(recordId);
        request.setRR(subDomain);
        request.setType(recordType);
        request.setValue(value);
        request.setTTL(600L); // 默认TTL 600秒

        UpdateDomainRecordResponse response = client.getAcsResponse(request);
        log.info("记录更新成功: {}", response.getRecordId());
        return true;
    }

    private boolean createNewRecord(String domain, String subDomain, String recordType, String value) 
            throws ClientException {
        log.info("创建新记录");
        AddDomainRecordRequest request = new AddDomainRecordRequest();
        request.setDomainName(domain);
        request.setRR(subDomain);
        request.setType(recordType);
        request.setValue(value);
        request.setTTL(600L); // 默认TTL 600秒

        AddDomainRecordResponse response = client.getAcsResponse(request);
        log.info("新记录创建成功: {}", response.getRecordId());
        return true;
    }

    @Override
    public String getCurrentRecord(String domain, String subDomain) {
        if (client == null) {
            log.warn("阿里云DNS服务未初始化，无法获取记录");
            return null;
        }

        try {
            log.debug("获取当前记录: domain={}, subDomain={}", domain, subDomain);
            DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
            request.setDomainName(domain);
            request.setRRKeyWord(subDomain);

            DescribeDomainRecordsResponse response = client.getAcsResponse(request);
            if (response.getDomainRecords() != null && !response.getDomainRecords().isEmpty()) {
                for (DescribeDomainRecordsResponse.Record record : response.getDomainRecords()) {
                    if (record.getRR().equals(subDomain)) {
                        log.debug("找到记录: {}", record.getValue());
                        return record.getValue();
                    }
                }
            }
            log.debug("未找到记录");
            return null;
        } catch (ClientException e) {
            log.error("获取当前记录失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getRecordId(String domain, String subDomain, String recordType) {
        try {
            DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
            request.setDomainName(domain);
            request.setRRKeyWord(subDomain);
            request.setType(recordType);

            DescribeDomainRecordsResponse response = client.getAcsResponse(request);
            if (response.getDomainRecords() != null && !response.getDomainRecords().isEmpty()) {
                for (DescribeDomainRecordsResponse.Record record : response.getDomainRecords()) {
                    if (record.getRR().equals(subDomain) && record.getType().equals(recordType)) {
                        return record.getRecordId();
                    }
                }
            }
        } catch (ClientException e) {
            log.error("获取记录ID失败: {}", e.getMessage(), e);
        }
        return null;
    }
}