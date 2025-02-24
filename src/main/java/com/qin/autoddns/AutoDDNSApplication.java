package com.qin.autoddns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoDDNSApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoDDNSApplication.class, args);
    }

}
