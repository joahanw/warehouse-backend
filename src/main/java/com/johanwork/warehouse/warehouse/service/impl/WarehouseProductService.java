package com.johanwork.warehouse.warehouse.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.product.service.IProductDomainService;
import com.johanwork.warehouse.warehouse.dto.*;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import com.johanwork.warehouse.warehouse.entity.WarehouseProduct;
import com.johanwork.warehouse.warehouse.mapper.WarehouseMapper;
import com.johanwork.warehouse.warehouse.mapper.WarehouseProductMapper;
import com.johanwork.warehouse.warehouse.repository.WarehouseProductRepository;
import com.johanwork.warehouse.warehouse.service.IWarehouseDomainService;
import com.johanwork.warehouse.warehouse.service.IWarehouseProductDomainService;
import com.johanwork.warehouse.warehouse.service.IWarehouseProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseProductService implements IWarehouseProductService
{

    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseProductMapper warehouseProductMapper;

    private final IWarehouseDomainService warehouseService;
    private final IProductDomainService productService;
    private final WarehouseMapper warehouseMapper;
    private final IWarehouseProductDomainService domainService;

    @Cacheable(
            value = "warehouse-product-list",
            condition = "#search == null || #search.isBlank()",
            key = "T(String).format('%d-%d-%s-%s', #pageNumber, #pageSize, #sortBy, #sortDirection)"
    )
    @Override
    public GenericResponse<PageResponse<WarehouseProductResponse>> getAllWarehouseProduct(int pageNumber, int pageSize,
                                                                                          String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return warehouseProductMapper.mapToPageGenericResponse(
                warehouseProductRepository.findAll(pageable),
                String.format(AppConstant.Success.FETCHED, "Warehouse Products")
        );
    }

    @Override
    public GenericResponse<List<WarehouseProductResponse>> getDetailWarehouseProductByWarehouseId(Long warehouseId) {
        var detail = domainService.findDetailByWarehouseId(warehouseId);
        if (detail.isEmpty()) {
            Warehouse warehouse = warehouseService.findWarehouseById(warehouseId);
            return new GenericResponse<>( List.of(
                    new WarehouseProductResponse(
                            null,
                            warehouseMapper.mapEntityToResponse(warehouse),
                            null,
                            null
                    )),
                    String.format(AppConstant.Success.FETCHED, "Warehouse Products")
            );
        }
        return warehouseProductMapper.mapToListGenericResponse(
                detail,
                String.format(AppConstant.Success.FETCHED, "Warehouse Products")
        );
    }

    @Override
    public GenericResponse<WarehouseProductResponse> getDetailWarehouseProductById(Long warehouseProductId) {
        WarehouseProduct warehouseProduct = domainService.findWarehouseProductById(warehouseProductId);
        return warehouseProductMapper.mapToGenericResponse(
                warehouseProduct,
                String.format(AppConstant.Success.FETCHED, "Warehouse Product")
        );
    }

    @Override
    public GenericResponse<WarehouseProductResponse> getWarehouseProductByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        WarehouseProduct warehouseProduct = domainService.findWarehouseProductByWarehouseIdAndProductId(warehouseId, productId);
        return warehouseProductMapper.mapToGenericResponse(
                warehouseProduct,
                String.format(AppConstant.Success.FETCHED, "Warehouse Product")
        );
    }

    @Override
    public GenericResponse<List<WarehouseResponse>> getWarehouseProductByProductId(Long productId) {
        List<Warehouse> warehouses = warehouseService.findWarehouseBaseProductId(productId);
        return warehouseMapper.mapToListGenericResponse(
                warehouses,
                String.format(AppConstant.Success.FETCHED, "Warehouses")
        );
    }

    @Override
    public GenericResponse<ProductTotalStockResponse> getProductTotalStock(Long productId) {
        ProductTotalStockResponse totalStock = warehouseProductRepository.getTotalStockByProductId(productId);
        return new GenericResponse<>(totalStock,
                String.format(AppConstant.Success.FETCHED,"Warehouse Product"));
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse-product-list", allEntries = true),
            @CacheEvict(value = "warehouses-product-by-warehouse-id-and-product-id", allEntries = true),
            @CacheEvict(value = "warehouses-product", allEntries = true),
            @CacheEvict(value = "warehouse-product-by-warehouse-id", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> create(WarehouseProductRequest request) {
        if (warehouseProductRepository.existsByWarehouseIdAndProductId(request.warehouseId(), request.productId())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    String.format(AppConstant.Error.TITLE_ALREADY_EXISTS, "Warehouse Product"),
                    String.format(AppConstant.Error.MESSAGE_ALREADY_EXISTS, "Warehouse Product")
            );
        }
        Warehouse warehouse = warehouseService.findWarehouseById(request.warehouseId());
        Product product = productService.findProductById(request.productId());
        WarehouseProduct wp = new WarehouseProduct();
        wp.setWarehouse(warehouse);
        wp.setProduct(product);
        wp.setStock(request.stock());
        warehouseProductRepository.save(wp);
        return warehouseProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.CREATED,"Warehouse Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse-product-list", allEntries = true),
            @CacheEvict(value = "warehouses-product-by-warehouse-id-and-product-id", allEntries = true),
            @CacheEvict(value = "warehouses-product", allEntries = true),
            @CacheEvict(value = "warehouse-product-by-warehouse-id", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> update(Long id, WarehouseProductRequest request) {
        WarehouseProduct wp = domainService.findWarehouseProductById(id);
        if (!Objects.equals(request.productId(), wp.getProduct().getId())){
            warehouseProductRepository.updateProductId(
                    request.stock(),
                    "LOGIN",
                    request.productId(),
                    request.warehouseId(),
                    id
            );
        }else {
            warehouseProductRepository.updateProductOnlyStock(
                    request.stock(),
                    "LOGIN",
                    request.warehouseId(),
                    id
            );
        }
        return warehouseProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.UPDATED,"Warehouse Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse-product-list", allEntries = true),
            @CacheEvict(value = "warehouses-product-by-warehouse-id-and-product-id", allEntries = true),
            @CacheEvict(value = "warehouses-product", allEntries = true),
            @CacheEvict(value = "warehouse-product-by-warehouse-id", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> delete(Long id) {
        WarehouseProduct wp = domainService.findWarehouseProductById(id);
        warehouseProductRepository.deleteById(id);
        return warehouseProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.DELETED,"Warehouse Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse-product-list", allEntries = true),
            @CacheEvict(value = "warehouses-product-by-warehouse-id-and-product-id", allEntries = true),
            @CacheEvict(value = "warehouses-product", allEntries = true),
            @CacheEvict(value = "warehouse-product-by-warehouse-id", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> deleteAllWarehouseProductByProductId(Long productId) {
        warehouseProductRepository.deleteWarehouseProductByProduct_Id(productId);
        return warehouseProductMapper.mapToGenericResponse(
                String.format(AppConstant.Success.DELETED_ALL, "Warehouse Product")
        );
    }

}
