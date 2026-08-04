package com.johanwork.warehouse.merchant.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantRequest;
import com.johanwork.warehouse.merchant.dto.MerchantResponse;
import com.johanwork.warehouse.merchant.dto.MerchantWithMerchantProductResponse;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.merchant.mapper.MerchantMapper;
import com.johanwork.warehouse.merchant.repository.MerchantRepository;
import com.johanwork.warehouse.merchant.service.IMerchantDomainService;
import com.johanwork.warehouse.merchant.service.IMerchantService;
import com.johanwork.warehouse.merchant.spesification.MerchantSpecification;
import com.johanwork.warehouse.user.entity.User;
import com.johanwork.warehouse.user.service.IUserDomainService;
import com.johanwork.warehouse.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MerchantService implements IMerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final IUserDomainService userService;
    private final IMerchantDomainService domainService;

    @Cacheable(
            value = "merchant-list",
            condition = "#search == null ||#search.isBlank() ",
            key = "T(String).format('%d-%d-%s-%s-%d', #pageNumber, #pageSize, #sortBy, #sortDirection, #keeperId)"
    )
    @Override
    public GenericResponse<PageResponse<MerchantWithMerchantProductResponse>> getAllMerchant(int pageNumber, int pageSize, String sortBy, String sortDirection, String search, Long keeperId) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Specification<Merchant> specification = MerchantSpecification.filter(keeperId, search);
        Page<Merchant> merchants = merchantRepository.findAll(specification, pageable);
        return merchantMapper.mapToMerchantWithMerchantProductResponse(merchants,
                String.format(AppConstant.Success.FETCHED, "Merchants")) ;
    }

    @Override
    public GenericResponse<MerchantResponse> getMerchantById(Long id) {
        MerchantResponse merchantResponse = merchantRepository.getMerchantById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "MERCHANT"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Merchant", id)));
        return merchantMapper.mapToMerchantResponse(merchantResponse,
                String.format(AppConstant.Success.FETCHED, "Merchant"));
    }

    @Caching(evict = {
            @CacheEvict(value = "merchant-list", allEntries = true),
            @CacheEvict(value = "merchants", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> create(MerchantRequest request) {
        Merchant merchant = merchantMapper.mapRequestToEntity(request);
        User user = userService.findById(request.keeperId());
        merchant.setKeeper(user);
        merchant.setCode("MCH-"+ UUID.randomUUID()
                .toString().substring(0, 8)
                .toUpperCase());
        merchantRepository.save(merchant);
        return merchantMapper.mapToGenericResponse(
                String.format(AppConstant.Success.CREATED, "Merchant"));
    }

    @Caching(evict = {
            @CacheEvict(value = "merchant-list", allEntries = true),
            @CacheEvict(value = "merchants", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> update(Long id, MerchantRequest request) {
        Merchant oldData = domainService.findMerchantById(id);
        Merchant merchant = merchantMapper.mapRequestToEntity(request);
        if (!Objects.equals(oldData.getKeeper().getId(), request.keeperId())){
            userService.findById(request.keeperId());
            merchant.setKeeper(userService.findById(request.keeperId()));
        }else {
            merchant.setKeeper(oldData.getKeeper());
        }
        merchant.setId(id);
        merchantRepository.save(merchant);
        return merchantMapper.mapToGenericResponse(
                String.format(AppConstant.Success.UPDATED, "Merchant"));
    }

    @Caching(evict = {
            @CacheEvict(value = "merchant-list", allEntries = true),
            @CacheEvict(value = "merchants", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> delete(Long id) {
        merchantRepository.delete(domainService.findMerchantById(id));
        return merchantMapper.mapToGenericResponse(
                String.format(AppConstant.Success.DELETED, "Merchant"));
    }

}


