package com.johanwork.warehouse.common.config.configProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "midtrans")
public record MidtransProperties(String serverKey, boolean isProduction) {
    public String baseUrl() {
        return isProduction
                ? "https://api.midtrans.com"
                : "https://api.sandbox.midtrans.com";
    }
}
