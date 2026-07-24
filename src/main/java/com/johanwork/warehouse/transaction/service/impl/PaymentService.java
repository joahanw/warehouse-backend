package com.johanwork.warehouse.transaction.service.impl;

import com.johanwork.warehouse.common.config.configProps.MidtransProperties;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.merchant.service.IMerchantProductDomainService;
import com.johanwork.warehouse.notification.dto.InvoiceEmailDto;
import com.johanwork.warehouse.notification.service.INotificationService;
import com.johanwork.warehouse.notification.service.ITelegramService;
import com.johanwork.warehouse.transaction.dto.FraudStatus;
import com.johanwork.warehouse.transaction.dto.PaymentMethod;
import com.johanwork.warehouse.transaction.dto.PaymentStatus;
import com.johanwork.warehouse.transaction.dto.response.MidtransNotification;
import com.johanwork.warehouse.transaction.entity.Transaction;
import com.johanwork.warehouse.transaction.repository.TransactionRepository;
import com.johanwork.warehouse.transaction.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.johanwork.warehouse.common.util.AppUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService {

    private final TransactionRepository transactionRepository;
    private final MidtransProperties midtransProperties;
    private final INotificationService notificationService;
    private final ITelegramService telegramService;
    private final IMerchantProductDomainService merchantProductDomainService;

    @Override
    public void handleMidtransHookPayment(MidtransNotification req) {
        // Verifikasi signature SHA-512
        String raw = req.orderId() + req.statusCode()
                + req.grossAmount() + midtransProperties.serverKey();

        if (!DigestUtils.sha512Hex(raw).equalsIgnoreCase(req.signatureKey())) {
            log.warn("Invalid signature for orderId={}", req.orderId());
            throw new CustomException(HttpStatus.FORBIDDEN,
                    AppConstant.Error.TITLE_FORBIDDEN,
                    AppConstant.Error.MESSAGE_FORBIDDEN);
        }

        Transaction transaction = transactionRepository.findByOrderId(req.orderId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND,"Transaction"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND,"Transaction", req.orderId())
                ));

        PaymentStatus paymentStatus = resolvePaymentStatus(req.transactionStatus());

        // Update status
        transaction.setPaymentStatus(paymentStatus);
        transaction.setFraudStatus(resolveFraudStatus(req.fraudStatus()));
        transaction.setTransactionCode(req.transactionId());
        transactionRepository.save(transaction);

        if (paymentStatus.name().equals("success")){
            transaction.getTransactionProducts().forEach(tp -> {
                merchantProductDomainService.reduceMerchantProductStock(transaction.getMerchant().getId(),
                        tp.getProduct().getId(),
                        tp.getQuantity().longValue());
            });
            notificationService.sendInvoiceEmail(transaction.getEmail(),
                    buildInvoiceData(transaction));
            telegramService.sendPaymentSuccess(transaction);
        }

        log.info("Notification: orderId={}, status={}", req.orderId(), req.transactionStatus());
    }

    private InvoiceEmailDto buildInvoiceData(Transaction transaction){
        String invoiceId = String.format("INV-"+transaction.getTransactionCode().substring(0,8)+"-00"+transaction.getMerchant().getId());
        List<InvoiceEmailDto.InvoiceItem> items = transaction.getTransactionProducts()
                .stream()
                .map(tp -> new InvoiceEmailDto.InvoiceItem(
                        tp.getProduct().getName(),
                        tp.getQuantity(),
                        formatCurrency(tp.getSubTotal())
                )).toList();

        return new InvoiceEmailDto(
                invoiceId,
                transaction.getName(),
                transaction.getEmail(),
                transaction.getOrderId(),
                LocalDateTime.now(),
                transaction.getSubTotal(),
                transaction.getTaxTotal(),
                transaction.getGrandTotal(),
                transaction.getShippingCost(),
                PaymentMethod.qris.name().toUpperCase(),
                transaction.getAddress(),
                items);
    }

    private PaymentStatus resolvePaymentStatus(String status) {
        return switch (status) {
            case "settlement" -> PaymentStatus.success;
            case "pending"    -> PaymentStatus.pending;
            case "deny"       -> PaymentStatus.failed;
            case "cancel"     -> PaymentStatus.cancel;
            case "expire"     -> PaymentStatus.expired;
            case "failure"    -> PaymentStatus.failed;
            default           -> null;
        };
    }

    private FraudStatus resolveFraudStatus(String status) {
        if (status == null) return null;
        return switch (status) {
            case "accept"    -> FraudStatus.accept;
            case "deny"      -> FraudStatus.deny;
            case "challenge" -> FraudStatus.challenge;
            default          -> null;
        };
    }

}

