package com.johanwork.warehouse.warehouse.service;

import com.johanwork.warehouse.warehouse.entity.WarehouseProduct;

import java.util.List;

public interface IWarehouseProductDomainService {
    List<WarehouseProduct> findDetailByWarehouseId(Long warehouseId);
    WarehouseProduct findWarehouseProductByWarehouseIdAndProductId(Long warehouseId, Long productId);
    WarehouseProduct findWarehouseProductById(Long id);
    void rebalanceQuantityStock(Long warehouseId, Long productId, int quantity, String status);
}
