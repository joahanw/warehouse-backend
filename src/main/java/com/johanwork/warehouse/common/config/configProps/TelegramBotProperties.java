package com.johanwork.warehouse.common.config.configProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public record TelegramBotProperties(
        String botToken,
        String baseUrl
) {
}
