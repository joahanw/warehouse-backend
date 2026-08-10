package com.johanwork.warehouse.transaction.service.impl;

import com.johanwork.warehouse.common.config.configProps.MidtransProperties;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.merchant.service.IMerchantProductDomainService;
import com.johanwork.warehouse.notification.dto.InvoiceEmailDto;
import com.johanwork.warehouse.notification.dto.WhatsAppTemplate;
import com.johanwork.warehouse.notification.entity.NotificationOutbox;
import com.johanwork.warehouse.notification.repository.NotificationOutboxRepository;
import com.johanwork.warehouse.notification.service.INotificationService;
import com.johanwork.warehouse.notification.service.ITelegramService;
import com.johanwork.warehouse.transaction.dto.FraudStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.johanwork.warehouse.common.util.AppUtil.formatCurrency;
import static com.johanwork.warehouse.common.util.AppUtil.formatDate;
import static com.johanwork.warehouse.common.util.AppUtil.toJson;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService {

    private final TransactionRepository transactionRepository;
    private final MidtransProperties midtransProperties;
    private final INotificationService notificationService;
    private final ITelegramService telegramService;
    private final IMerchantProductDomainService merchantProductDomainService;
    private final NotificationOutboxRepository notificationOutboxRepository;

    @Transactional
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

        PaymentStatus incomingStatus = resolvePaymentStatus(req.transactionStatus());

        if (incomingStatus == null) {
            log.warn("Unhandled transaction_status={} for orderId={}, notification ignored",
                    req.transactionStatus(), req.orderId());
            return;
        }

        confirm(transaction, incomingStatus, resolveFraudStatus(req.fraudStatus()), req.transactionId());

        log.info("Notification: orderId={}, status={}", req.orderId(), req.transactionStatus());
    }

    @Transactional
    @Override
    public void confirm(Transaction transaction, PaymentStatus newStatus, FraudStatus fraudStatus, String transactionCodeOverride) {
        if (transaction.getPaymentStatus() == newStatus) {
            log.info("Duplicate confirmation ignored: orderId={}, status={} already applied",
                    transaction.getOrderId(), newStatus);
            return;
        }

        if (transaction.getPaymentStatus() == PaymentStatus.success) {
            log.warn("Confirmation ignored: orderId={} already marked success, incoming status={}",
                    transaction.getOrderId(), newStatus);
            return;
        }

        transaction.setPaymentStatus(newStatus);
        if (fraudStatus != null) {
            transaction.setFraudStatus(fraudStatus);
        }
        if (transactionCodeOverride != null) {
            transaction.setTransactionCode(transactionCodeOverride);
        }
        transactionRepository.save(transaction);

        if (newStatus == PaymentStatus.success) {
            transaction.getTransactionProducts().forEach(tp ->
                    merchantProductDomainService.reduceMerchantProductStock(transaction.getMerchant().getId(),
                            tp.getProduct().getId(),
                            tp.getQuantity().longValue()));
            if (!transaction.getPhone().isBlank()){
                var params = List.of(formatCurrency(transaction.getGrandTotal()),
                        formatDate(transaction.getDeliveryDate()));
                notificationOutboxRepository.save(new NotificationOutbox(
                        transaction.getPhone(),
                        WhatsAppTemplate.PAYMENT_CONFIRMED_V1,
                        toJson(params)
                ));
            }
//            notificationService.sendInvoiceEmail(transaction.getEmail(), buildInvoiceData(transaction));
            telegramService.sendPaymentSuccess(transaction);
        }

        log.info("Payment confirmed: orderId={}, status={}", transaction.getOrderId(), newStatus);
    }

    private InvoiceEmailDto buildInvoiceData(Transaction transaction) {
        String invoiceId = String.format("INV-" + transaction.getTransactionCode().substring(0, 8) + "-00" + transaction.getMerchant().getId());
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
                transaction.getPaymentMethod().name().toUpperCase(),
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
