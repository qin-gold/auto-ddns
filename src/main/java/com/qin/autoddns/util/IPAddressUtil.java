package com.qin.autoddns.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * IP地址工具类
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
public class IPAddressUtil {
    private static final Logger logger = LoggerFactory.getLogger(IPAddressUtil.class);
    
    // IP地址验证正则表达式
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    
    // 连接超时时间（毫秒）
    private static final int CONNECT_TIMEOUT = 5000;
    // 读取超时时间（毫秒）
    private static final int READ_TIMEOUT = 5000;

    // 定义多个提供公网IP查询的服务
    private static final String[] IP_SERVICES = {
            "http://checkip.amazonaws.com",
            "http://icanhazip.com",
            "http://ifconfig.me/ip",
            "http://ipinfo.io/ip"
    };

    // 缓存上一次获取的公网IP地址
    private static String cachedIP = null;

    /**
     * 验证IP地址格式是否正确
     *
     * @param ip IP地址
     * @return 是否是有效的IP地址
     */
    public static boolean isValidIP(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 尝试从多个服务获取公网IP地址，并更新缓存
     *
     * @return 公网IP地址，如果所有服务都不可用则返回 null
     */
    public static String getPublicIP() {
        for (String service : IP_SERVICES) {
            String ip = getIPFromService(service);
            if (ip != null) {
                updateIPCache(ip);
                return ip;
            }
        }
        logger.error("无法从任何服务获取公网IP地址");
        return null;
    }

    /**
     * 从指定服务获取IP地址
     *
     * @param service 服务URL
     * @return IP地址，如果获取失败返回null
     */
    private static String getIPFromService(String service) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URI(service).toURL().openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String ip = reader.readLine();
                if (StringUtils.hasText(ip)) {
                    ip = ip.trim();
                    if (isValidIP(ip)) {
                        return ip;
                    } else {
                        logger.warn("从服务 {} 获取到无效的IP地址: {}", service, ip);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("服务 {} 不可用: {}", service, e.getMessage());
        } catch (URISyntaxException e) {
            logger.error("服务URL格式错误: {}", service, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * 更新IP地址缓存
     *
     * @param newIP 新的IP地址
     */
    private static void updateIPCache(String newIP) {
        if (!newIP.equals(cachedIP)) {
            logger.info("IP地址发生变化: {} -> {}", cachedIP, newIP);
            cachedIP = newIP;
        }
    }

    /**
     * 获取当前缓存的IP地址
     *
     * @return 缓存的IP地址，如果没有则返回null
     */
    public static String getCachedIP() {
        return cachedIP;
    }
}