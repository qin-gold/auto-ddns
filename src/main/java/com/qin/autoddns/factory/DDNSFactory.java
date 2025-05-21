package com.qin.autoddns.factory;

import com.qin.autoddns.enums.DNSProvider;
import com.qin.autoddns.service.DDNSService;
import com.qin.autoddns.service.impl.CloudflareDDNSServiceImpl;
import com.qin.autoddns.service.impl.TencentDDNSServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class DDNSFactory {

    private final TencentDDNSServiceImpl tencentDDNSService;
    private final CloudflareDDNSServiceImpl cloudflareDDNSService;

    public DDNSFactory(TencentDDNSServiceImpl tencentDDNSService,
                       CloudflareDDNSServiceImpl cloudflareDDNSService) {
        this.tencentDDNSService = tencentDDNSService;
        this.cloudflareDDNSService = cloudflareDDNSService;
    }

    public DDNSService createDDNSService(String type) {
        DNSProvider provider = DNSProvider.fromCode(type);
        return switch (provider) {
            case TENCENT -> tencentDDNSService;
            case CLOUDFLARE -> cloudflareDDNSService;
            case ALIYUN ->
                    throw new UnsupportedOperationException(provider.getDesc() + " DNS service not implemented yet");
        };
    }
} 