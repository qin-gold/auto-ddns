package com.qin.autoddns;

import com.qin.autoddns.config.DDNSConfig;
import com.qin.autoddns.config.EmailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoDDNSApplication {

    private static final Logger log = LoggerFactory.getLogger(AutoDDNSApplication.class);

    private final DDNSConfig ddnsConfig;
    private final EmailConfig emailConfig;
    private final Environment environment;

    public AutoDDNSApplication(DDNSConfig ddnsConfig, EmailConfig emailConfig, Environment environment) {
        this.ddnsConfig = ddnsConfig;
        this.emailConfig = emailConfig;
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(AutoDDNSApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void printConfigInfo() {
        String[] activeProfiles = environment.getActiveProfiles();
        log.info("==========================================");
        log.info("DDNS服务启动配置信息：");
        log.info("------------------------------------------");
        log.info("当前激活的配置: {}", activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "默认配置");
        log.info("DNS服务提供商: {}", ddnsConfig.getProvider());
        log.info("域名: {}.{}", ddnsConfig.getSubDomain(), ddnsConfig.getDomain());
        log.info("更新间隔: {} 毫秒", ddnsConfig.getUpdateInterval());
        log.info("------------------------------------------");

        // 检查邮件配置是否在当前激活的配置中
        boolean emailEnabled = emailConfig.isEnabled() &&
                (activeProfiles.length == 0 || environment.getProperty("spring.mail.enabled", Boolean.class, false));

        log.info("邮件通知功能: {}", emailEnabled ? "已启用" : "已禁用");
        if (emailEnabled) {
            log.info("SMTP服务器: {}:{}", emailConfig.getHost(), emailConfig.getPort());
            log.info("发件人: {}", emailConfig.getFrom());
            log.info("收件人: {}", emailConfig.getTo());
        }
        log.info("==========================================");
    }
}
