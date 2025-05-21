package com.qin.autoddns.service;

import com.qin.autoddns.config.DDNSConfig;
import com.qin.autoddns.factory.DDNSFactory;
import com.qin.autoddns.util.IPAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * DDNS更新服务
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
@Service
public class DDNSUpdateService {
    private final Logger log = LoggerFactory.getLogger(DDNSUpdateService.class);
    private final DDNSConfig config;
    private final DDNSService ddnsService;
    private final EmailService emailService;

    public DDNSUpdateService(DDNSConfig config, DDNSFactory ddnsFactory, EmailService emailService) {
        this.config = config;
        this.ddnsService = ddnsFactory.createDDNSService(config.getProvider());
        this.emailService = emailService;
        log.info("DDNS服务已启动，当前配置：");
        log.info("域名: {}.{}", config.getSubDomain(), config.getDomain());
        log.info("服务提供商: {}", config.getProvider());
        log.info("更新间隔: {} 毫秒", config.getUpdateInterval());
    }

    @Scheduled(fixedRateString = "${ddns.update-interval}")
    public void updateDNSRecord() {
        String currentIp = IPAddressUtil.getPublicIP();
        if (currentIp != null) {
            String currentRecord = ddnsService.getCurrentRecord(config.getDomain(), config.getSubDomain());
            String fullDomain = config.getSubDomain() + "." + config.getDomain();

            log.info("检查DNS记录: {}", fullDomain);
            log.info("当前公网IP: {}", currentIp);
            log.info("当前DNS记录: {}", currentRecord != null ? currentRecord : "无记录");

            if (!currentIp.equals(currentRecord)) {
                log.info("IP地址发生变化，开始更新DNS记录...");
                boolean success = ddnsService.updateDNSRecord(
                        config.getDomain(),
                        config.getSubDomain(),
                        "A",
                        currentIp
                );
                if (success) {
                    log.info("DNS记录更新成功:");
                    log.info("域名: {}", fullDomain);
                    log.info("新IP: {}", currentIp);
                    log.info("原IP: {}", currentRecord != null ? currentRecord : "无历史记录");
                    emailService.sendDNSUpdateNotification(true, fullDomain, currentRecord, currentIp, null);
                } else {
                    log.error("DNS记录更新失败:");
                    log.error("域名: {}", fullDomain);
                    log.error("目标IP: {}", currentIp);
                    log.error("当前IP: {}", currentRecord);
                    emailService.sendDNSUpdateNotification(false, fullDomain, currentRecord, currentIp, "DNS API调用失败");
                }
            } else {
                log.info("IP地址未发生变化，无需更新");
            }
        } else {
            log.error("无法获取当前公网IP地址");
            emailService.sendDNSUpdateNotification(false,
                    config.getSubDomain() + "." + config.getDomain(),
                    "未知", "未知",
                    "无法获取当前公网IP地址");
        }
    }
} 