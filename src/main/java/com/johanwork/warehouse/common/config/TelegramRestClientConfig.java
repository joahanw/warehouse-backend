package com.johanwork.warehouse.common.config;

import com.johanwork.warehouse.common.config.configProps.TelegramBotProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class TelegramRestClientConfig {

    @Bean("telegramRestClient")
    public RestClient telegramRestClient(TelegramBotProperties props) {
        return RestClient.builder()
                .baseUrl(props.baseUrl() + "/bot" + props.botToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
