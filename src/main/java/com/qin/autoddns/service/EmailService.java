package com.qin.autoddns.service;

import com.qin.autoddns.config.EmailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
@Service
public class EmailService {
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailConfig emailConfig;
    private final JavaMailSender mailSender;

    public EmailService(EmailConfig emailConfig, JavaMailSender mailSender) {
        this.emailConfig = emailConfig;
        this.mailSender = mailSender;
    }

    /**
     * 发送DNS更新结果通知
     *
     * @param success  是否成功
     * @param domain   域名
     * @param oldIp    原IP
     * @param newIp    新IP
     * @param errorMsg 错误信息（如果有）
     */
    public void sendDNSUpdateNotification(boolean success, String domain, String oldIp, String newIp, String errorMsg) {
        if (!emailConfig.isEnabled()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getFrom());
            message.setTo(emailConfig.getTo());

            if (success) {
                message.setSubject("DDNS更新成功通知 - " + domain);
                message.setText(String.format("""
                        DDNS记录更新成功！
                        
                        域名: %s
                        原IP: %s
                        新IP: %s
                        
                        此邮件为自动发送，请勿回复。
                        """, domain, oldIp, newIp));
            } else {
                message.setSubject("DDNS更新失败警告 - " + domain);
                message.setText(String.format("""
                        DDNS记录更新失败！
                        
                        域名: %s
                        当前IP: %s
                        目标IP: %s
                        错误信息: %s
                        
                        请检查系统日志获取详细信息。
                        此邮件为自动发送，请勿回复。
                        """, domain, oldIp, newIp, errorMsg));
            }

            mailSender.send(message);
            log.info("发送{}通知邮件成功", success ? "成功" : "失败");
        } catch (Exception e) {
            log.error("发送通知邮件失败", e);
        }
    }
} 