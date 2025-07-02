package com.qin.autoddns.factory;

import com.qin.autoddns.enums.DNSProvider;
import com.qin.autoddns.service.DDNSService;
import com.qin.autoddns.service.impl.AliyunDDNSServiceImpl;
import com.qin.autoddns.service.impl.CloudflareDDNSServiceImpl;
import com.qin.autoddns.service.impl.TencentDDNSServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class DDNSFactory {

    private final TencentDDNSServiceImpl tencentDDNSService;
    private final CloudflareDDNSServiceImpl cloudflareDDNSService;
    private final AliyunDDNSServiceImpl aliyunDDNSService;

    public DDNSFactory(TencentDDNSServiceImpl tencentDDNSService,
                       CloudflareDDNSServiceImpl cloudflareDDNSService,
                       AliyunDDNSServiceImpl aliyunDDNSService) {
        this.tencentDDNSService = tencentDDNSService;
        this.cloudflareDDNSService = cloudflareDDNSService;
        this.aliyunDDNSService = aliyunDDNSService;
    }

    public DDNSService createDDNSService(String type) {
        DNSProvider provider = DNSProvider.fromCode(type);
        return switch (provider) {
            case TENCENT -> tencentDDNSService;
            case CLOUDFLARE -> cloudflareDDNSService;
            case ALIYUN -> aliyunDDNSService;
        };
    }
} 