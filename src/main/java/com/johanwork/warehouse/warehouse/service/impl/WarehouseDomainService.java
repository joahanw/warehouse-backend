package com.johanwork.warehouse.warehouse.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import com.johanwork.warehouse.warehouse.repository.WarehouseRepository;
import com.johanwork.warehouse.warehouse.service.IWarehouseDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseDomainService implements IWarehouseDomainService {

    private final WarehouseRepository warehouseRepository;

    @Override
    public List<Warehouse> findWarehouseBaseProductId(Long id) {
        return warehouseRepository.getWarehouseByProductId(id);
    }

    @Cacheable(value = "warehouses", key = "#id")
    @Override
    public Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "WAREHOUSE"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Warehouse", id)
                ));
    }
}
