package com.qin.autoddns.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qin.autoddns.config.DDNSConfig;
import com.qin.autoddns.service.DDNSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Cloudflare DNS服务实现
 */
@Service
public class CloudflareDDNSServiceImpl implements DDNSService {

    private final Logger log = LoggerFactory.getLogger(CloudflareDDNSServiceImpl.class);
    private final DDNSConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String zoneId;
    private String recordId;

    public CloudflareDDNSServiceImpl(DDNSConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        try {
            validateConfig();
            initializeZoneId();
            log.info("Cloudflare DNS服务初始化成功");
        } catch (Exception e) {
            log.error("Cloudflare DNS服务初始化失败: {}", e.getMessage());
            throw new RuntimeException("DNS服务初始化失败", e);
        }
    }

    private void validateConfig() {
        if (config.getAccessKey() == null || config.getAccessKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Cloudflare API Token 不能为空");
        }
        log.info("API Token长度: {}", config.getAccessKey().length());
    }

    private void initializeZoneId() {
        try {
            HttpHeaders headers = createHeaders();
            String url = "https://api.cloudflare.com/client/v4/zones?name=" + config.getDomain();

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.get("success").asBoolean()) {
                JsonNode zones = root.get("result");
                if (zones.isArray() && !zones.isEmpty()) {
                    this.zoneId = zones.get(0).get("id").asText();
                    log.info("成功获取Zone ID: {}", zoneId);
                } else {
                    throw new RuntimeException("未找到域名对应的Zone");
                }
            } else {
                throw new RuntimeException("获取Zone ID失败: " + root.get("errors").toString());
            }
        } catch (Exception e) {
            log.error("初始化Zone ID失败: {}", e.getMessage());
            throw new RuntimeException("初始化Zone ID失败", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getAccessKey());
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Override
    public boolean updateDNSRecord(String domain, String subDomain, String recordType, String value) {
        try {
            log.info("开始更新DNS记录: domain={}, subDomain={}, type={}, value={}",
                    domain, subDomain, recordType, value);

            if (recordId == null) {
                recordId = getRecordId(domain, subDomain);
            }

            if (recordId != null) {
                return updateExistingRecord(domain, subDomain, recordType, value);
            } else {
                return createNewRecord(domain, subDomain, recordType, value);
            }
        } catch (Exception e) {
            log.error("更新DNS记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean updateExistingRecord(String domain, String subDomain, String recordType, String value) {
        try {
            String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s",
                    zoneId, recordId);

            String requestBody = String.format("""
                    {
                        "type": "%s",
                        "name": "%s",
                        "content": "%s",
                        "ttl": 1,
                        "proxied": false
                    }
                    """, recordType, subDomain + "." + domain, value);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, requestEntity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.get("success").asBoolean()) {
                log.info("记录更新成功");
                return true;
            } else {
                log.error("记录更新失败: {}", root.get("errors").toString());
                return false;
            }
        } catch (Exception e) {
            log.error("更新记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean createNewRecord(String domain, String subDomain, String recordType, String value) {
        try {
            String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records", zoneId);

            String requestBody = String.format("""
                    {
                        "type": "%s",
                        "name": "%s",
                        "content": "%s",
                        "ttl": 1,
                        "proxied": false
                    }
                    """, recordType, subDomain + "." + domain, value);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.get("success").asBoolean()) {
                this.recordId = root.get("result").get("id").asText();
                log.info("新记录创建成功");
                return true;
            } else {
                log.error("创建记录失败: {}", root.get("errors").toString());
                return false;
            }
        } catch (Exception e) {
            log.error("创建记录失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getCurrentRecord(String domain, String subDomain) {
        try {
            log.debug("获取当前记录: domain={}, subDomain={}", domain, subDomain);

            String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records?name=%s.%s",
                    zoneId, subDomain, domain);

            HttpHeaders headers = createHeaders();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.get("success").asBoolean()) {
                JsonNode records = root.get("result");
                if (records.isArray() && !records.isEmpty()) {
                    String value = records.get(0).get("content").asText();
                    log.debug("找到记录: {}", value);
                    return value;
                }
            }
            log.debug("未找到记录");
            return null;
        } catch (Exception e) {
            log.error("获取当前记录失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getRecordId(String domain, String subDomain) {
        try {
            String url = String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records?name=%s.%s",
                    zoneId, subDomain, domain);

            HttpHeaders headers = createHeaders();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.get("success").asBoolean()) {
                JsonNode records = root.get("result");
                if (records.isArray() && !records.isEmpty()) {
                    return records.get(0).get("id").asText();
                }
            }
        } catch (Exception e) {
            log.error("获取记录ID失败: {}", e.getMessage(), e);
        }
        return null;
    }
} 