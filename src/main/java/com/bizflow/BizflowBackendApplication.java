package com.bizflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BizflowBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BizflowBackendApplication.class, args);
    }
}