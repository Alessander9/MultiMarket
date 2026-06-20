package com.multimarket.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.multimarket.clients")
public class OpenFeignConfig {
}
