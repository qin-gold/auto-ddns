package com.qin.autoddns.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DDNS配置类
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
@Configuration
@ConfigurationProperties(prefix = "ddns")
public class DDNSConfig {
    private String provider;
    private String accessKey;
    private String secretKey;
    private String domain;
    private String subDomain;
    private long updateInterval = 300000; // 默认5分钟

    // Getters and Setters
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }
} 