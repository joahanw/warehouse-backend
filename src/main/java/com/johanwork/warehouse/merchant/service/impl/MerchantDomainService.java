package com.johanwork.warehouse.merchant.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.merchant.repository.MerchantRepository;
import com.johanwork.warehouse.merchant.service.IMerchantDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantDomainService implements IMerchantDomainService {

    private final MerchantRepository merchantRepository;

    @Cacheable(value = "merchants", key = "'id:' + #id")
    @Override
    public Merchant findMerchantById(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "MERCHANT"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Merchant", id)));
    }

    @Cacheable(value = "merchants", key = "'userId:' + #userId")
    @Override
    public Merchant findMerchantByUserId(Long userId) {
        return merchantRepository.findByKeeper_Id(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "MERCHANT"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Merchant", userId)));
    }
}
