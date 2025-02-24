package com.qin.autoddns.factory;

import com.qin.autoddns.enums.DNSProvider;
import com.qin.autoddns.service.DDNSService;
import com.qin.autoddns.service.impl.TencentDDNSServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class DDNSFactory {
    
    private final TencentDDNSServiceImpl tencentDDNSService;

    public DDNSFactory(TencentDDNSServiceImpl tencentDDNSService) {
        this.tencentDDNSService = tencentDDNSService;
    }

    public DDNSService createDDNSService(String type) {
        DNSProvider provider = DNSProvider.fromCode(type);
        return switch (provider) {
            case TENCENT -> tencentDDNSService;
            case ALIYUN -> throw new UnsupportedOperationException(provider.getDesc() + " DNS service not implemented yet");
        };
    }
} 