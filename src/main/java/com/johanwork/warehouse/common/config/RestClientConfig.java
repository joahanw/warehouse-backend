package com.johanwork.warehouse.common.config;

import com.johanwork.warehouse.common.config.configProps.MidtransProperties;
import com.johanwork.warehouse.common.config.configProps.TelegramBotProperties;
import com.johanwork.warehouse.common.config.configProps.WahaAppProperties;
import com.johanwork.warehouse.common.config.configProps.WhatsAppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Configuration
public class RestClientConfig {

    @Bean(name = "midtransRestClient")
    public RestClient midtransRestClient(MidtransProperties props){
        String credentials = Base64.getEncoder()
                .encodeToString((props.serverKey() + ":").getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(20));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }

    @Bean("telegramRestClient")
    public RestClient telegramRestClient(TelegramBotProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .baseUrl(props.baseUrl() + "/bot" + props.botToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }

    @Bean("whatsappRestClient")
    public RestClient whatsappRestClient(WhatsAppProperties props){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(5));
        factory.setConnectTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .baseUrl(props.apiBaseURL()+"/"+props.phoneNumberId())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer "+ props.accessToken())
                .requestFactory(factory)
                .build();
    }

    @Bean("wahaRestClient")
    public RestClient wahaRestClient(WahaAppProperties props){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(20));
        factory.setConnectTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .baseUrl(props.apiBaseURL())
                .defaultHeader("X-Api-Key", props.apiKey())
                .requestFactory(factory)
                .build();
    }
}
