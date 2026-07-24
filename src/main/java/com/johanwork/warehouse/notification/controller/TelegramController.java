package com.johanwork.warehouse.notification.controller;

import com.johanwork.warehouse.merchant.repository.MerchantRepository;
import com.johanwork.warehouse.notification.dto.TelegramUpdate;
import com.johanwork.warehouse.notification.service.ITelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramController {

    private final ITelegramService telegramService;
    private final MerchantRepository merchantRepository;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleUpdate(@RequestBody TelegramUpdate update) {
        if (update.message() == null || update.message().text() == null) {
            return ResponseEntity.ok().build();
        }

        Long   chatId = update.message().chat().id();
        String text   = update.message().text().trim();
        String name   = update.message().from().firstName();

        // Get merchant ChatID for telegram user
        if ("/start".equals(text)) {
            telegramService.sendMessage(chatId.toString(),
                    "👋 Halo, <b>" + name + "</b>!\n\n" +
                            "Untuk mengaktifkan notifikasi, kirim kode merchant kamu.\n\n" +
                            "Contoh: <code>/daftar ABC123</code>"
            );

        } else if (text.startsWith("/daftar ")) {
            String merchantCode = text.replace("/daftar ", "").trim();
            telegramService.handleMerchantRegistration(chatId, merchantCode, name);
        }

        return ResponseEntity.ok().build();
    }

}
