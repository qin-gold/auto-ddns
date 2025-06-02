package com.qin.autoddns.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RestTemplateRetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateRetryAspect.class);

    private final RetryTemplate retryTemplate;

    public RestTemplateRetryAspect(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    @Around("execution(* org.springframework.web.client.RestTemplate.exchange(..))")
    public Object retryOnRestTemplateCall(ProceedingJoinPoint joinPoint) throws Throwable {
        return retryTemplate.execute(context -> {
            try {
                log.debug("执行 RestTemplate 请求 (尝试次数: {})", context.getRetryCount() + 1);
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                log.warn("RestTemplate 调用失败，准备重试...", throwable);
                throw throwable; // 触发重试机制
            }
        });
    }
}
