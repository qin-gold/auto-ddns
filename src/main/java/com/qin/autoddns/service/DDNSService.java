package com.qin.autoddns.service;

/**
 * DDNS服务接口
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
public interface DDNSService {
    /**
     * 更新DNS记录
     * @param domain 域名
     * @param subDomain 子域名
     * @param recordType 记录类型(A, AAAA等)
     * @param value IP地址
     * @return 是否更新成功
     */
    boolean updateDNSRecord(String domain, String subDomain, String recordType, String value);
    
    /**
     * 获取当前DNS记录
     * @param domain 域名
     * @param subDomain 子域名
     * @return 当前记录值
     */
    String getCurrentRecord(String domain, String subDomain);
} 