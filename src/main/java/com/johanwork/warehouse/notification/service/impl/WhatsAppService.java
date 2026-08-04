package com.johanwork.warehouse.notification.service.impl;

import com.johanwork.warehouse.common.config.configProps.WhatsAppProperties;
import com.johanwork.warehouse.notification.service.IWhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppService implements IWhatsAppService {

    private final RestClient restClient;
    private final WhatsAppProperties props;

    public WhatsAppService(@Qualifier("whatsappRestClient") RestClient whatsappRestClient,
                           WhatsAppProperties props) {
        this.restClient = whatsappRestClient;
        this.props = props;
    }

    @Override
    public void sendPaymentCreated(String toPhone, String qrImageUrl, String invoiceNumber, String customerName, String totalAmount, String dueDate) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", toPhone,
                "type", "template",
                "template", Map.of(
                        "name", props.template().paymentCreated(),
                        "language", Map.of("code", "id"),
                        "components", List.of(
                                Map.of(
                                        "type", "header",
                                        "parameters", List.of(
                                                Map.of("type", "image",
                                                        "image", Map.of("link", qrImageUrl))
                                        )
                                ),
                                Map.of(
                                        "type", "body",
                                        "parameters", List.of(
                                                Map.of("type", "text", "text", customerName),
                                                Map.of("type", "text", "text", invoiceNumber),
                                                Map.of("type", "text", "text", totalAmount),
                                                Map.of("type", "text", "text", dueDate)
                                        )
                                )
                        )
                )
        );

        try {
            String response = restClient.post()
                    .uri("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            log.info("WA payment notification sent to {}: {}", toPhone, response);
        } catch (Exception e) {
            log.error("Failed to send WA notification to {}", toPhone, e);
        }
    }

    @Override
    public void sendFreeFormText(String toPhone, String message) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", toPhone,
                "type", "text",
                "text", Map.of("body", message)
        );

        restClient.post()
                .uri("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
    }
}
