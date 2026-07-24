package com.johanwork.warehouse.warehouse.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.warehouse.dto.WarehouseRequest;
import com.johanwork.warehouse.warehouse.dto.WarehouseResponse;

public interface IWarehouseService {
    GenericResponse<PageResponse<WarehouseResponse>> getAllWarehouse(int pageNumber, int pageSize, String sortBy, String sortDirection, String search);
    GenericResponse<WarehouseResponse> getWarehouseById(Long id);
    GenericResponse<Void> createWarehouse(WarehouseRequest warehouseRequest);
    GenericResponse<Void> updateWarehouse(Long id, WarehouseRequest warehouseRequest);
    GenericResponse<Void> deleteWarehouse(Long id);
}
