package com.johanwork.warehouse.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QrisChargeRequest(
        @JsonProperty("payment_type") String paymentType,
        @JsonProperty("transaction_details") TransactionDetail transactionDetails,
        @JsonProperty("customer_details") CustomerDetail customerDetails,
        @JsonProperty("item_details") List<ItemDetail> itemDetails,
        @JsonProperty("custom_expiry") CustomExpiry customExpiry
) {
    public record CustomExpiry(
            @JsonProperty("expiry_duration") int duration,
            @JsonProperty("unit") String unit
    ) {}
    public record TransactionDetail(
           @JsonProperty("order_id") String orderId,
           @JsonProperty("gross_amount") long grossAmount
    ) {}

    public record CustomerDetail(
            @JsonProperty("first_name") String firstName,
            @JsonProperty("email") String email,
            @JsonProperty("phone") String phone
    ) {}

    public record ItemDetail(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("price") long price,
            @JsonProperty("quantity") int quantity
    ) {}
}