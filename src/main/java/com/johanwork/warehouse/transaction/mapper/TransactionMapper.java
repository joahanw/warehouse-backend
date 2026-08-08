package com.johanwork.warehouse.transaction.mapper;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.transaction.dto.PaymentMethod;
import com.johanwork.warehouse.transaction.dto.PaymentStatus;
import com.johanwork.warehouse.transaction.dto.request.TransactionRequest;
import com.johanwork.warehouse.transaction.dto.response.CreateTransactionResponse;
import com.johanwork.warehouse.transaction.dto.response.DashboardStatsByMerchantResponse;
import com.johanwork.warehouse.transaction.dto.response.DashboardStatsResponse;
import com.johanwork.warehouse.transaction.dto.response.TransactionResponse;
import com.johanwork.warehouse.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final TransactionProductMapper transactionProductMapper;

    public Transaction requestToEntity(TransactionRequest rq, Merchant merchant, BigDecimal subTotal,
                                       BigDecimal taxTotal, BigDecimal grandTotal, String orderId){
        Transaction tx = new Transaction();
        tx.setName(rq.name());
        tx.setEmail(rq.email());
        tx.setAddress(rq.address());
        tx.setPhone(rq.phone());
        tx.setOrderId(orderId);
        tx.setCurrency("IDR");
        tx.setNotes(rq.notes());
        tx.setDeliveryDate(rq.deliveryDate());
        tx.setMerchant(merchant);
        tx.setSubTotal(subTotal);
        tx.setTaxTotal(taxTotal);
        tx.setGrandTotal(grandTotal);
        tx.setShippingCost(rq.shippingCost());
        tx.setPaymentStatus(PaymentStatus.pending);
        tx.setPaymentMethod(resolvePaymentMethod(rq.paymentMethod()));
        return tx;
    }

    private PaymentMethod resolvePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PaymentMethod.qris;
        }
        try {
            return PaymentMethod.valueOf(paymentMethod.trim());
        } catch (IllegalArgumentException e) {
            return PaymentMethod.qris;
        }
    }

    public TransactionResponse entityToResponse(Transaction transaction, boolean includeTransactionProducts){
        return new TransactionResponse(
                transaction.getId(),
                transaction.getName(),
                transaction.getPhone(),
                transaction.getEmail(),
                transaction.getAddress(),
                transaction.getSubTotal(),
                transaction.getTaxTotal(),
                transaction.getGrandTotal(),
                transaction.getMerchant().getId(),
                transaction.getMerchant().getName(),
                transaction.getPaymentStatus().name(),
                transaction.getPaymentMethod().name(),
                transaction.getTransactionCode(),
                transaction.getOrderId(),
                transaction.getNotes(),
                transaction.getDeliveryDate(),
                includeTransactionProducts
                        ? transaction.getTransactionProducts().stream()
                            .map(transactionProductMapper::entityToResponse).toList()
                        : null
        );
    }

    public GenericResponse<DashboardStatsResponse> mapToDashboardResponse(DashboardStatsResponse res, String message){
        return new GenericResponse<>(res, message);
    }

    public GenericResponse<DashboardStatsByMerchantResponse> mapToDashboardStatsByMerchantResponse(DashboardStatsByMerchantResponse res, String message){
        return new GenericResponse<>(res, message);
    }

    public GenericResponse<CreateTransactionResponse> mapToCreateTransactionResponse(Long transactionId, String orderId,String qrCodeUrl,
                                                                                     String expiryTime, BigDecimal grandTotal,
                                                                                     LocalDate deliveryDate,
                                                                                     String message){
        return new GenericResponse<>(new CreateTransactionResponse(
                transactionId,
                orderId,
                qrCodeUrl,
                expiryTime,
                grandTotal,
                deliveryDate
        ), message);
    }

    public GenericResponse<PageResponse<TransactionResponse>> mapToPageTransactionResponse(Page<Transaction> res, String message){
        if (!res.isEmpty()){
            return new GenericResponse<>(
                    new PageResponse(
                            res.map(data -> this.entityToResponse(data, true)).getContent(),
                            res.getNumber(),
                            res.getSize(),
                            res.getTotalElements(),
                            res.getTotalPages(),
                            res.hasNext(),
                            res.hasPrevious()
                    ),
                    message
            );
        }
        return new GenericResponse<>(new PageResponse<>(), message);
    }


}
