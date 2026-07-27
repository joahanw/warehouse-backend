package com.johanwork.warehouse.merchant.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import com.johanwork.warehouse.merchant.repository.MerchantProductRepository;
import com.johanwork.warehouse.merchant.service.IMerchantDomainService;
import com.johanwork.warehouse.merchant.service.IMerchantProductDomainService;
import com.johanwork.warehouse.product.service.IProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantProductDomainService implements IMerchantProductDomainService {

    private final MerchantProductRepository merchantProductRepository;
    private final IProductDomainService productService;
    private final IMerchantDomainService merchantService;

    @Caching(evict = {
            @CacheEvict(value = "merchant-list", allEntries = true),
            @CacheEvict(value = "merchants", allEntries = true)
    })
    @Transactional
    @Override
    public void reduceMerchantProductStock(Long merchantId, Long productId, Long quantity) {
        productService.findProductById(productId);
        merchantService.findMerchantById(merchantId);
        merchantProductRepository.reduceMerchantProductStock(merchantId, productId, quantity);
    }

    @Cacheable(value = "merchant-product", key = "#id")
    @Override
    public MerchantProduct findMerchantProductById(Long id) {
        return merchantProductRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "MERCHANT PRODUCT"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND,"Merchant Product", id)));
    }

    @Cacheable(value = "merchant-product", key = "#merchantId + '-' + #productId")
    @Override
    public MerchantProduct findMerchantProductByMerchantIdAndProductId(Long merchantId, Long productId) {
        return merchantProductRepository.findByProduct_IdAndMerchant_Id(productId, merchantId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "Merchant Product"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Merchant Product", merchantId + " - " + productId)));
    }
}
