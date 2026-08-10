package com.johanwork.warehouse.merchant.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantProductRequest;
import com.johanwork.warehouse.merchant.dto.MerchantProductResponse;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import com.johanwork.warehouse.merchant.mapper.MerchantProductMapper;
import com.johanwork.warehouse.merchant.repository.MerchantProductRepository;
import com.johanwork.warehouse.merchant.service.IMerchantDomainService;
import com.johanwork.warehouse.merchant.service.IMerchantProductDomainService;
import com.johanwork.warehouse.merchant.service.IMerchantProductService;
import com.johanwork.warehouse.merchant.spesification.MerchantProductSpecification;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.product.service.IProductDomainService;
import com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import com.johanwork.warehouse.warehouse.service.IWarehouseDomainService;
import com.johanwork.warehouse.warehouse.service.IWarehouseProductDomainService;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MerchantProductService implements IMerchantProductService {

    private final MerchantProductRepository merchantProductRepository;
    private final MerchantProductMapper merchantProductMapper;

    private final IProductDomainService productService;
    private final IMerchantDomainService merchantService;
    private final IWarehouseDomainService warehouseService;
    private final IWarehouseProductDomainService warehouseProductService;
    private final IMerchantProductDomainService domainService;

    @Cacheable(
            value = "merchant-product-list",
            key = "T(String).format('%d-%d-%s-%s-%s-%s-%s', #pageNumber, #pageSize, #sortBy, #sortDirection, #stock, #merchantId, #productId)"
    )
    @Override
    public GenericResponse<PageResponse<MerchantProductResponse>> getMerchantProducts(int pageNumber, int pageSize,
                                                                                      String sortBy, String sortDirection,
                                                                                      Integer stock, Long merchantId, Long productId) {
        Sort sort = sortBy.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Specification<MerchantProduct> specification =
                MerchantProductSpecification.filter(stock, merchantId, productId);
        Page<MerchantProduct> merchantProducts = merchantProductRepository.findAll(specification, pageable);
        return merchantProductMapper.mapToPageGenericResponse(
                merchantProducts,
                String.format(AppConstant.Success.FETCHED, "Merchant Products")
        );
    }

    @Override
    public GenericResponse<MerchantProductResponse> getMerchantProductById(Long id) {
        return merchantProductMapper.mapToGenericResponse(
                domainService.findMerchantProductById(id),
                String.format(AppConstant.Success.FETCHED, "Merchant Product")
        );
    }

    @Cacheable(value = "merchant-product", key = "#merchantId + #barcode")
    @Override
    public GenericResponse<MerchantProductResponse> getMerchantProductByBarcode(Long merchantId, String barcode) {
        Product product = productService.findProductByBarcode(barcode);
        merchantService.findMerchantById(merchantId);
        MerchantProduct merchantProduct = domainService.findMerchantProductByMerchantIdAndProductId(merchantId, product.getId());
        return merchantProductMapper.mapToGenericResponse(
                merchantProduct,
                String.format(AppConstant.Success.FETCHED, "Merchant Product")
        );
    }

    @Override
    public GenericResponse<ProductTotalStockResponse> getTotalStock(Long productId) {
        productService.findProductById(productId);
        return merchantProductMapper.mapToProductStockResponse(
                merchantProductRepository.getTotalStockByProductId(productId),
                String.format(AppConstant.Success.FETCHED, "Merchant Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "merchant-product-list", allEntries = true),
            @CacheEvict(value = "merchant-product", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> create(MerchantProductRequest request) {
        Merchant merchant = merchantService.findMerchantById(request.merchantId());
        Product product = productService.findProductById(request.productId());
        Warehouse warehouse = warehouseService.findWarehouseById(request.warehouseId());
        MerchantProduct merchantProduct = merchantProductMapper.mapRequestToEntity(merchant, product, warehouse, request.stock());
        warehouseProductService.rebalanceQuantityStock(warehouse.getId(), product.getId(), request.stock(), "REDUCE");
        merchantProductRepository.save(merchantProduct);
        return merchantProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.CREATED, "Merchant Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "merchant-product-list", allEntries = true),
            @CacheEvict(value = "merchant-product", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> update(Long id, MerchantProductRequest request) {
        MerchantProduct merchantProduct = domainService.findMerchantProductById(id);
        if (!merchantProduct.getMerchant().getId().equals(request.merchantId())){
            merchantProduct.setMerchant(merchantService.findMerchantById(request.merchantId()));
        }
        if (!merchantProduct.getProduct().getId().equals(request.productId())){
            merchantProduct.setProduct(productService.findProductById(request.productId()));
        }
        if (!merchantProduct.getWarehouse().getId().equals(request.warehouseId())){
            merchantProduct.setWarehouse(warehouseService.findWarehouseById(request.warehouseId()));
        }
        if (!merchantProduct.getStock().equals(request.stock())){
            if (merchantProduct.getStock() > request.stock()){
                warehouseProductService.rebalanceQuantityStock(merchantProduct.getWarehouse().getId(), merchantProduct.getProduct().getId(),
                        merchantProduct.getStock() - request.stock(), "ADD");
            }else {
                warehouseProductService.rebalanceQuantityStock(merchantProduct.getWarehouse().getId(), merchantProduct.getProduct().getId(),
                        request.stock() - merchantProduct.getStock(), "REDUCE");
            }
            merchantProduct.setStock(request.stock());
        }
        return merchantProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.UPDATED, "Merchant Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "merchant-product-list", allEntries = true),
            @CacheEvict(value = "merchant-product", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> delete(Long id) {
        MerchantProduct merchantProduct = domainService.findMerchantProductById(id);
        if (merchantProduct.getStock()>0){
            warehouseProductService.rebalanceQuantityStock(merchantProduct.getWarehouse().getId(),
                    merchantProduct.getProduct().getId(), merchantProduct.getStock(), "ADD");
        }
        merchantProductRepository.delete(merchantProduct);
        return merchantProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.DELETED, "Merchant Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "merchant-product-list", allEntries = true),
            @CacheEvict(value = "merchant-product", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> deleteAllByProductId(Long productId) {
        Product product = productService.findProductById(productId);
        merchantProductRepository.deleteMerchantProductByProduct_Id(productId);
        return merchantProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.DELETED_ALL, "Merchant Product")
        );
    }

}
