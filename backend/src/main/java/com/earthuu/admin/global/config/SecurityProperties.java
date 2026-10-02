package com.earthuu.admin.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(List<String> allowedOrigins) {
    public SecurityProperties {
        allowedOrigins = allowedOrigins == null ? List.of("http://localhost:5173") : List.copyOf(allowedOrigins);
    }
}
