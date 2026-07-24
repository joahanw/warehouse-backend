package com.johanwork.warehouse.merchant.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantProductRequest;
import com.johanwork.warehouse.merchant.dto.MerchantProductResponse;
import com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse;

public interface IMerchantProductService {
    GenericResponse<PageResponse<MerchantProductResponse>> getMerchantProducts(int pageNumber, int pageSize, String sortBy, String sortDirection, Integer stock, Long merchantId, Long productId);
    GenericResponse<MerchantProductResponse> getMerchantProductById(Long id);
    GenericResponse<MerchantProductResponse> getMerchantProductByBarcode(Long merchantId, String barcode);

    GenericResponse<ProductTotalStockResponse> getTotalStock(Long productId);

    GenericResponse<Void> create(MerchantProductRequest request);
    GenericResponse<Void> update(Long id, MerchantProductRequest request);
    GenericResponse<Void> delete(Long id);
    GenericResponse<Void> deleteAllByProductId(Long productId);
}
