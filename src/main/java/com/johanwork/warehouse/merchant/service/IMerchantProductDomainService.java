package com.johanwork.warehouse.merchant.service;

import com.johanwork.warehouse.merchant.entity.MerchantProduct;

public interface IMerchantProductDomainService {
    void reduceMerchantProductStock(Long merchantId, Long productId, Long quantity);
    MerchantProduct findMerchantProductById(Long id);
    MerchantProduct findMerchantProductByMerchantIdAndProductId(Long merchantId, Long productId);
}
