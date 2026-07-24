package com.johanwork.warehouse.transaction.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.transaction.dto.response.TransactionProductResponse;
import com.johanwork.warehouse.transaction.entity.TransactionProduct;

public interface ITransactionProductService {
    GenericResponse<PageResponse<TransactionProductResponse>> getAllTransaction(int pageNumber, int pageSize,
                                                                                String sortBy, String sortDirection,
                                                                                String search, Long merchantId);
    GenericResponse<TransactionProductResponse> getByTransactionId(Long transactionId);
}
