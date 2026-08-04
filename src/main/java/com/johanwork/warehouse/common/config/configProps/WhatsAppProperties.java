package com.johanwork.warehouse.common.config.configProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp")
public record WhatsAppProperties(
        String apiBaseURL,
        String phoneNumberId,
        String accessToken,
        Template template
) {
    public record Template(String paymentCreated){}
}
