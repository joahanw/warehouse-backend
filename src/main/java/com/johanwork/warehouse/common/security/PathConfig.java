package com.johanwork.warehouse.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PathConfig {

    @Bean(name = "publicPaths")
    public List<String> publicPaths() {
        return List.of(
                "/api/auth/**",
                "/api/csrf-token",
                "/api/payments/notification",
                "/api/logging/public",
                "/api/swagger-ui.html",
                "/swagger-ui/**",
                "/api/v3/api-docs/**",
                "/swagger-resources/**",
                "/swagger-ui.html",
                "/webjars/**",
                "/api/telegram/webhook",
                "/warehouse/actuator/**"
        );
    }

    @Bean(name = "securedPaths")
    public List<String> securedPaths(){
        return List.of(
                "/api/**"
        );
    }

}
