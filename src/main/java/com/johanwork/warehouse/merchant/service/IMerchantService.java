package com.johanwork.warehouse.merchant.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantRequest;
import com.johanwork.warehouse.merchant.dto.MerchantResponse;
import com.johanwork.warehouse.merchant.dto.MerchantWithMerchantProductResponse;

import java.util.List;

public interface IMerchantService {
    GenericResponse<PageResponse<MerchantWithMerchantProductResponse>> getAllMerchant(int pageNumber, int pageSize, String sortBy, String sortDirection, String search, Long keeperId);
    GenericResponse<MerchantResponse> getMerchantById(Long id);
    GenericResponse<Void> create(MerchantRequest request);
    GenericResponse<Void> update(Long id, MerchantRequest request);
    GenericResponse<Void> delete(Long id);
}
