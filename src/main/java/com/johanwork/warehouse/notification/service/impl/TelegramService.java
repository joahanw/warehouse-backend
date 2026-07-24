package com.johanwork.warehouse.notification.service.impl;

import com.johanwork.warehouse.common.util.AppUtil;
import com.johanwork.warehouse.merchant.repository.MerchantRepository;
import com.johanwork.warehouse.notification.service.ITelegramService;
import com.johanwork.warehouse.transaction.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.johanwork.warehouse.common.util.AppUtil.formatCurrency;
import static com.johanwork.warehouse.common.util.AppUtil.formatDateTime;

@Service
@Slf4j
public class TelegramService implements ITelegramService {

    private final RestClient telegramRestClient;
    private final MerchantRepository merchantRepository;

    public TelegramService(@Qualifier("telegramRestClient") RestClient telegramRestClient,
                           MerchantRepository merchantRepository) {
        this.telegramRestClient = telegramRestClient;
        this.merchantRepository = merchantRepository;
    }

    public void handleMerchantRegistration(Long chatId, String merchantCode, String name) {
        merchantRepository.findByCode(merchantCode).ifPresentOrElse(
                merchant -> {
                    merchant.setTelegramChatId(chatId.toString());
                    merchantRepository.save(merchant);

                    sendMessage(chatId.toString(),
                            "✅ <b>Berhasil terdaftar!</b>\n\n" +
                                    "Halo, <b>" + name + "</b>!\n" +
                                    "Notifikasi pembayaran akan masuk ke chat ini."
                    );

                    log.info("Merchant {} registered Telegram chatId={}", merchantCode, chatId);
                },
                () -> sendMessage(chatId.toString(),
                        "❌ <b>Kode merchant tidak ditemukan.</b>\n\n" +
                                "Pastikan kode yang kamu masukkan benar.\n" +
                                "Hubungi admin untuk mendapatkan kode merchant."
                )
        );
    }

    @Async("notificationTaskExecutor")
    public void sendPaymentSuccess(Transaction transaction) {
        String chatId = transaction.getMerchant().getTelegramChatId();

        if (chatId == null || chatId.isBlank()) {
            log.warn("Merchant {} tidak memiliki Telegram Chat ID",
                    transaction.getMerchant().getId());
            return;
        }

        sendMessage(chatId, buildMessage(transaction));
    }

    public void sendMessage(String chatId, String text) {
        try {
            telegramRestClient.post()
                    .uri("/sendMessage")
                    .body(Map.of(
                            "chat_id",    chatId,
                            "text",       text,
                            "parse_mode", "HTML"
                    ))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Telegram sent: chatId={}", chatId);

        } catch (Exception e) {
            log.error("Failed to send Telegram: chatId={}, error={}", chatId, e.getMessage());
        }
    }

    private String buildMessage(Transaction transaction) {
        // Build item list
        StringBuilder itemList = new StringBuilder();
        transaction.getTransactionProducts().forEach(tp ->
                itemList.append(String.format(
                        "  • %s x%d — %s\n",
                        tp.getProduct().getName(),
                        tp.getQuantity(),
                        formatCurrency(tp.getSubTotal())
                ))
        );

        return String.format(
                "✅ <b>PEMBAYARAN BERHASIL</b>\n"          +
                        "━━━━━━━━━━━━━━━━━━━━\n"                  +
                        "🧾 Order ID : <code>%s</code>\n"         +
                        "👤 Customer : %s\n"                       +
                        "📞 Phone    : %s\n"                       +
                        "━━━━━━━━━━━━━━━━━━━━\n"                  +
                        "🛍 <b>Detail Pesanan</b>\n"               +
                        "%s"                                       +
                        "━━━━━━━━━━━━━━━━━━━━\n"                  +
                        "🧮 Subtotal  : %s\n"                     +
                        "🚚 Pengiriman: %s\n"                     +
                        "💰 <b>Total  : %s</b>\n"                 +
                        "💳 Metode    : QRIS\n"                   +
                        "━━━━━━━━━━━━━━━━━━━━\n"                  +
                        "⏰ %s",
                transaction.getOrderId(),
                transaction.getName(),
                transaction.getPhone() != null ? transaction.getPhone() : "-",
                itemList,
                formatCurrency(transaction.getSubTotal()),
                formatCurrency(Objects.requireNonNullElse(
                        transaction.getShippingCost(), BigDecimal.ZERO)),
                formatCurrency(transaction.getGrandTotal()),
                formatDateTime(LocalDateTime.now())
        );
    }

}
