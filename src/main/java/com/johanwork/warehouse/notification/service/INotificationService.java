package com.johanwork.warehouse.notification.service;

import com.johanwork.warehouse.notification.dto.InvoiceEmailDto;
import com.johanwork.warehouse.notification.dto.PaymentPendingDto;

public interface INotificationService {
    void sendWelcomeEmail(String toEmail, String customerName);
    void sendPaymentPendingEmail(String toEmail, PaymentPendingDto req);
    void sendInvoiceEmail(String toEmail, InvoiceEmailDto req);
}
