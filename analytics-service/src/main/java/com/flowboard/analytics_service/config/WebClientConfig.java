package com.flowboard.analytics_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient;

// Here we are returning web-client builder to build webclient
@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public Builder webClientBuilder() {
        return WebClient.builder();
    }
}