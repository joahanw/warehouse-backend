package com.johanwork.warehouse.merchant.service;

import com.johanwork.warehouse.merchant.entity.Merchant;

public interface IMerchantDomainService {
    Merchant findMerchantById(Long id);
    Merchant findMerchantByUserId(Long userId);
}
