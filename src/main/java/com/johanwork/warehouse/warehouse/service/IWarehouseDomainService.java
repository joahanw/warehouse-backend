package com.johanwork.warehouse.warehouse.service;

import com.johanwork.warehouse.warehouse.entity.Warehouse;

import java.util.List;
import java.util.Optional;

public interface IWarehouseDomainService {
    List<Warehouse> findWarehouseBaseProductId(Long id);
    Warehouse findWarehouseById(Long id);
}
