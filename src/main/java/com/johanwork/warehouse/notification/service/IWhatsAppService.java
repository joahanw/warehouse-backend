package com.johanwork.warehouse.notification.service;

public interface IWhatsAppService {
    void sendPaymentCreated(String toPhone, String qrImageUrl,
                            String invoiceNumber, String customerName,
                            String totalAmount, String dueDate);
    void sendFreeFormText(String toPhone, String message);
}
