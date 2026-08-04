package com.johanwork.warehouse.transaction.service;

import com.johanwork.warehouse.transaction.dto.FraudStatus;
import com.johanwork.warehouse.transaction.dto.PaymentStatus;
import com.johanwork.warehouse.transaction.dto.response.MidtransNotification;
import com.johanwork.warehouse.transaction.entity.Transaction;

public interface IPaymentService {
    void handleMidtransHookPayment(MidtransNotification req);
    void confirm(Transaction transaction, PaymentStatus newStatus, FraudStatus fraudStatus, String transactionCodeOverride);
}
