package com.paymentflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public String paymentMode(@Value("${payment.model:learning}") String mode) {
        return mode;
    }
}
