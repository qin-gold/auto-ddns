package com.qin.autoddns.enums;

/**
 * DNS服务提供商枚举
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
public enum DNSProvider {
    TENCENT("tencent", "腾讯云"),
    ALIYUN("aliyun", "阿里云"),
    CLOUDFLARE("cloudflare", "Cloudflare");

    private final String code;
    private final String desc;

    DNSProvider(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DNSProvider fromCode(String code) {
        for (DNSProvider provider : values()) {
            if (provider.getCode().equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unsupported DNS provider: " + code);
    }
} 