package com.johanwork.warehouse.warehouse.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.warehouse.entity.WarehouseProduct;
import com.johanwork.warehouse.warehouse.repository.WarehouseProductRepository;
import com.johanwork.warehouse.warehouse.service.IWarehouseProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseProductDomainService implements IWarehouseProductDomainService {

    private final WarehouseProductRepository warehouseProductRepository;

    @Cacheable(value = "warehouses-product-by-warehouse-id", key = "#warehouseId")
    @Override
    public List<WarehouseProduct> findDetailByWarehouseId(Long warehouseId) {
        return warehouseProductRepository.getDetailByWarehouseId(warehouseId);
    }

    @Cacheable(value = "warehouses-product-by-warehouse-id-and-product-id", key = "#warehouseId + '-' + #productId")
    @Override
    public WarehouseProduct findWarehouseProductByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        return warehouseProductRepository.getWarehouseProductByWarehouseIdAndProductId(warehouseId, productId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "WAREHOUSE PRODUCT"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Warehouse Product", warehouseId + " and " + productId)
                ));
    }

    @Cacheable(value = "warehouses-product", key = "#id")
    @Override
    public WarehouseProduct findWarehouseProductById(Long id) {
        return warehouseProductRepository.getDetailById(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "WAREHOUSE PRODUCT"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Warehouse Product", id)
                ));
    }

    @Transactional
    @Override
    public void rebalanceQuantityStock(Long warehouseId, Long productId, int quantity, String status) {
        WarehouseProduct warehouseProduct = findWarehouseProductByWarehouseIdAndProductId(warehouseId, productId);
        if (status.equals("ADD")){
            warehouseProduct.setStock(warehouseProduct.getStock() + quantity);
        }else {
            if (warehouseProduct.getStock() < quantity){
                throw new CustomException(
                        HttpStatus.BAD_REQUEST,
                        String.format(AppConstant.Error.TITLE_INSUFFICIENT_STOCK, "Warehouse Product"),
                        String.format(AppConstant.Error.MESSAGE_INSUFFICIENT_STOCK, productId, "warehouse " + warehouseId)
                );
            }
            warehouseProduct.setStock(warehouseProduct.getStock() - quantity);
        }
    }
}
