package com.johanwork.warehouse.transaction.service;

import com.johanwork.warehouse.transaction.dto.response.MidtransNotification;

public interface IPaymentService {
    void handleMidtransHookPayment(MidtransNotification req);
}
