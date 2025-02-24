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

    public TencentDDNSServiceImpl(DDNSConfig config) {
        Credential cred = new Credential(config.getAccessKey(), config.getSecretKey());
        this.client = new DnspodClient(cred, "");
    }

    @Override
    public boolean updateDNSRecord(String domain, String subDomain, String recordType, String value) {
        try {
            // 先获取记录ID
            String recordId = getRecordId(domain, subDomain);

            if (recordId != null) {
                // 更新已存在的记录
                ModifyRecordRequest request = new ModifyRecordRequest();
                request.setDomain(domain);
                request.setRecordId(Long.parseLong(recordId));
                request.setRecordType(recordType);
                request.setRecordLine("默认");
                request.setValue(value);
                request.setSubDomain(subDomain);

                 client.ModifyRecord(request);
            } else {
                // 创建新记录
                CreateRecordRequest request = new CreateRecordRequest();
                request.setDomain(domain);
                request.setRecordType(recordType);
                request.setRecordLine("默认");
                request.setValue(value);
                request.setSubDomain(subDomain);

                client.CreateRecord(request);
            }
            return true;
        } catch (TencentCloudSDKException e) {
            log.error("出现异常=>", e);
            return false;
        }
    }

    @Override
    public String getCurrentRecord(String domain, String subDomain) {
        try {
            DescribeRecordListRequest request = new DescribeRecordListRequest();
            request.setDomain(domain);

            DescribeRecordListResponse response = client.DescribeRecordList(request);
            for (RecordListItem record : response.getRecordList()) {
                if (record.getName().equals(subDomain)) {
                    return record.getValue();
                }
            }
        } catch (TencentCloudSDKException e) {
            log.error("出现异常=>", e);
        }
        return null;
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
            log.error("出现异常=>", e);
        }
        return null;
    }
} 