package com.johanwork.warehouse.transaction.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.transaction.dto.request.TransactionRequest;
import com.johanwork.warehouse.transaction.dto.response.CreateTransactionResponse;
import com.johanwork.warehouse.transaction.dto.response.DashboardStatsByMerchantResponse;
import com.johanwork.warehouse.transaction.dto.response.TransactionResponse;

public interface ITransactionService {
    GenericResponse<DashboardStatsByMerchantResponse> getDashboardStats(String email);
    GenericResponse<CreateTransactionResponse> create(TransactionRequest transactionRequest);
    GenericResponse<PageResponse<TransactionResponse>> getAllTransaction(int pageNumber, int pageSize, String sortBy, String sortDirection, String search, Long merchantId);
}
