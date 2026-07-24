package com.johanwork.warehouse.warehouse.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.warehouse.dto.WarehouseRequest;
import com.johanwork.warehouse.warehouse.dto.WarehouseResponse;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import com.johanwork.warehouse.warehouse.mapper.WarehouseMapper;
import com.johanwork.warehouse.warehouse.repository.WarehouseProductRepository;
import com.johanwork.warehouse.warehouse.repository.WarehouseRepository;
import com.johanwork.warehouse.warehouse.service.IWarehouseDomainService;
import com.johanwork.warehouse.warehouse.service.IWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService implements IWarehouseService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final IWarehouseDomainService domainService;

    @Cacheable(
            value = "warehouse-list",
            condition = "#search == null || #search.isBlank()",
            key = "T(String).format('%d-%d-%s-%s', #pageNumber, #pageSize, #sortBy, #sortDirection)"
    )
    @Override
    public GenericResponse<PageResponse<WarehouseResponse>> getAllWarehouse(int pageNumber, int pageSize,
                                                                           String sortBy, String sortDirection,
                                                                           String search) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<WarehouseResponse> warehouses = null == search
                ? warehouseRepository.getAllWarehouses(pageable)
                : warehouseRepository.getAllWarehousesByNameOrAddressOrPhone(search, pageable);
        return warehouseMapper.mapToPageResponse(warehouses,
                String.format(AppConstant.Success.FETCHED,"Warehouses"));
    }

    @Override
    public GenericResponse<WarehouseResponse> getWarehouseById(Long id) {
        return warehouseMapper.mapToGenericResponse(domainService.findWarehouseById(id),
                String.format(AppConstant.Success.FETCHED, "Warehouse"));
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse-list", allEntries = true),
            @CacheEvict(value = "warehouses", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> createWarehouse(WarehouseRequest warehouseRequest) {
        Warehouse warehouse = warehouseMapper.mapRequestToEntity(warehouseRequest);
        warehouseRepository.save(warehouse);
        return warehouseMapper.mapToGenericResponse(
                String.format(AppConstant.Success.CREATED, "Warehouse"));
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse-list", allEntries = true),
            @CacheEvict(value = "warehouses", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> updateWarehouse(Long id, WarehouseRequest warehouseRequest) {
        domainService.findWarehouseById(id);
        Warehouse warehouse = warehouseMapper.mapRequestToEntity(warehouseRequest);
        warehouse.setId(id);
        warehouseRepository.save(warehouse);
        return warehouseMapper.mapToGenericResponse(
                String.format(AppConstant.Success.UPDATED, "Warehouse"));
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse-list", allEntries = true),
            @CacheEvict(value = "warehouses", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> deleteWarehouse(Long id) {
        Warehouse warehouse = domainService.findWarehouseById(id);
        if (warehouseProductRepository.existsByWarehouseIdAndStockGreaterThan(id,0)){
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    AppConstant.Error.TITLE_WAREHOUSE_CANNOT_DELETE,
                    AppConstant.Error.MESSAGE_WAREHOUSE_CANNOT_DELETE);
        }
        warehouseRepository.delete(warehouse);
        return warehouseMapper.mapToGenericResponse(
                String.format(AppConstant.Success.DELETED, "Warehouse"));
    }

}
