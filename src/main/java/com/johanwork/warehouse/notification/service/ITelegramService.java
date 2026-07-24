package com.johanwork.warehouse.notification.service;

import com.johanwork.warehouse.transaction.entity.Transaction;

public interface ITelegramService {
    void sendPaymentSuccess(Transaction transaction);
    void sendMessage(String chatId, String message);
    void handleMerchantRegistration(Long chatId, String merchantCode, String name);

}
