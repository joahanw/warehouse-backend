package com.johanwork.warehouse.common.config.configProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "waha")
public record WahaAppProperties(
        String apiBaseURL,
        String apiKey,
        String session,
        String enable,
        String hmacKey,
        long minDelayMs,
        long maxDelayMs
) {
}
