package com.johanwork.warehouse.transaction.controller;

import com.johanwork.warehouse.transaction.dto.response.MidtransNotification;
import com.johanwork.warehouse.transaction.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final IPaymentService paymentService;

    @PostMapping("/notification")
    public ResponseEntity<Void> handleNotification(@RequestBody MidtransNotification notif) {
        paymentService.handleMidtransHookPayment(notif);
        return ResponseEntity.ok().build();
    }

}
