package com.johanwork.warehouse.warehouse.mapper;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.warehouse.dto.WarehouseRequest;
import com.johanwork.warehouse.warehouse.dto.WarehouseResponse;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WarehouseMapper implements GenericResponseMapper<Warehouse, WarehouseRequest, WarehouseResponse> {

    @Override
    public WarehouseResponse mapEntityToResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getPhoto(),
                warehouse.getPhone(),
                Long.valueOf(0)
        );
    }

    @Override
    public Warehouse mapRequestToEntity(WarehouseRequest warehouseRequest) {
        return new Warehouse(
                null,
                warehouseRequest.name(),
                warehouseRequest.address(),
                warehouseRequest.photo(),
                warehouseRequest.phone(),
                null,
                null
        );
    }

    @Override
    public List<WarehouseResponse> mapListEntityToListResponse(List<Warehouse> m) {
        if (m != null){
            return m.stream().map(this::mapEntityToResponse).toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<WarehouseResponse> mapPageEntityToPageResponse(Page<Warehouse> m) {
        if (m != null){
            return new PageResponse<>(
                    m.map(this::mapEntityToResponse).getContent(),
                    m.getNumber(),
                    m.getSize(),
                    m.getTotalElements(),
                    m.getTotalPages(),
                    m.hasNext(),
                    m.hasPrevious()
            );
        }
        return new PageResponse<>();
    }

    public GenericResponse<PageResponse<WarehouseResponse>> mapToPageResponse(Page<WarehouseResponse> res, String message){
        if (null != res){
            var response = new PageResponse<>(
                    res.getContent(),
                    res.getNumber(),
                    res.getSize(),
                    res.getTotalElements(),
                    res.getTotalPages(),
                    res.hasNext(),
                    res.hasPrevious()
            );
            return new GenericResponse<>(response, message);
        }
        return new GenericResponse<>(new PageResponse<>(), message);
    }
}
