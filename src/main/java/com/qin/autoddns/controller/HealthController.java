package com.qin.autoddns.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 *
 * @author qinshijiao
 * @since 2024/03/21
 */
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
} 