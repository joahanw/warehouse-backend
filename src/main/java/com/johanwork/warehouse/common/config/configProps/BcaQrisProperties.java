package com.johanwork.warehouse.common.config.configProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bca.qris")
public record BcaQrisProperties(String staticCode) {
}
