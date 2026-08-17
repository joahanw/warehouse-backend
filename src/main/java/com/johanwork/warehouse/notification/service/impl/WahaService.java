package com.johanwork.warehouse.notification.service.impl;

import com.johanwork.warehouse.common.config.configProps.WahaAppProperties;
import com.johanwork.warehouse.common.config.configProps.WhatsAppProperties;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.notification.dto.WhatsAppTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WahaService {

    private final RestClient client;
    private final String session;

    public WahaService(@Qualifier("wahaRestClient") RestClient restClient,
                       WahaAppProperties props) {
        this.client = restClient;
        this.session = props.session();
    }

    record SendTextRequest(String session, String chatId, String text) {}
    record ContactCheckResponse(boolean numberExists, String chatId){}

    public void sendTemplate(String target, WhatsAppTemplate template, List<String> params) {
        dispatch(toChatId(target), template.render(params), template.name());
    }

    public void sendText(String target, String text) {
        dispatch(toChatId(target), text, "RAW");
    }

    /**
     * Cek dulu apakah nomor terdaftar di WhatsApp sebelum kirim. Mengirim berulang kali
     * ke nomor yang tidak terdaftar adalah salah satu pemicu ban yang paling sering.
     */
    public boolean numberExists(String phoneNumber) {
        try {
            ContactCheckResponse response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/contacts/check-exists")
                            .queryParam("phone", phoneNumber)
                            .queryParam("session", session)
                            .build())
                    .retrieve()
                    .body(ContactCheckResponse.class);
            return response != null && response.numberExists();
        } catch (RestClientException ex) {
            log.warn("Gagal cek keberadaan nomor {}: {}", phoneNumber, ex.getMessage());
            return true; // gagal cek bukan alasan untuk memblokir pengiriman
        }
    }

    private void dispatch(String chatId, String text, String label) {
        Map<?, ?> response = client.post()
                .uri("/api/sendText")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SendTextRequest(session, chatId, text))
                .retrieve()
                .body(Map.class);

        Object id = response == null ? null : response.get("id");
        log.info("WAHA successfully send message {} template={} messageId={}", chatId, label, id);
    }

    public void sendImageUrl(String target, String imageUrl, String caption){
        String chatId = toChatId(target);
        log.info("This the number {}", chatId);

        var file = Map.of(
                "mimetype", "image/png",
                "url", imageUrl,
                "filename", "qris.png"
        );

        var payload = Map.of(
                "session", session,
                "chatId", chatId,
                "file", file,
                "caption", caption
        );

        try {
            Map<?,?> response = client.post()
                    .uri("/api/sendImage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            Object id = response == null ? null : response.get("id");
            log.info("WAHA successfully send image {} messageId={}", chatId, id);
        } catch (RestClientException ex) {
            log.error("Failed to send image : {}", ex.getMessage());
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                    AppConstant.Error.TITLE_QR_IMAGE_UNAVAILABLE,
                    AppConstant.Error.MESSAGE_QR_IMAGE_UNAVAILABLE);
        }
    }

    /** Nomor -> 628xxx@c.us. chatId grup (@g.us) atau kontak (@c.us) diteruskan apa adanya. */
    static String toChatId(String target) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("Target kosong");
        }
        if (target.endsWith("@g.us") || target.endsWith("@c.us")) {
            return target;
        }
        return target + "@c.us";
    }

}
