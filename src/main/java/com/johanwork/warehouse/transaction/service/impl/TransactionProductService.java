package com.johanwork.warehouse.transaction.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.transaction.dto.response.TransactionProductResponse;
import com.johanwork.warehouse.transaction.entity.TransactionProduct;
import com.johanwork.warehouse.transaction.mapper.TransactionProductMapper;
import com.johanwork.warehouse.transaction.repository.TransactionProductRepository;
import com.johanwork.warehouse.transaction.service.ITransactionProductService;
import com.johanwork.warehouse.transaction.spesification.TransactionProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProductService implements ITransactionProductService {

    private final TransactionProductRepository transactionProductRepository;
    private final TransactionProductMapper transactionProductMapper;

    @Override
    public GenericResponse<PageResponse<TransactionProductResponse>> getAllTransaction(int pageNumber, int pageSize, String sortBy,
                                                                                       String sortDirection, String search,
                                                                                       Long merchantId) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<TransactionProduct> transactionProducts = transactionProductRepository.findAll(
                TransactionProductSpecification.filter(search, merchantId),
                pageable
        );
        return transactionProductMapper.mapToPageTransactionProductResponse(transactionProducts,
                String.format(AppConstant.Success.FETCHED, "Transaction Product"));
    }

    @Override
    public GenericResponse<TransactionProductResponse> getByTransactionId(Long transactionId) {
        TransactionProduct transactionProduct = transactionProductRepository.findByTransaction_Id(transactionId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "TRANSACTION"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Transaction", transactionId)
                ));
        return transactionProductMapper.mapToGenericResponse(transactionProduct,
                String.format(AppConstant.Success.FETCHED, "Transaction Product"));
    }
}
