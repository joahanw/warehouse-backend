package com.johanwork.warehouse.warehouse.mapper;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.product.dto.ProductResponse;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.product.mapper.ProductMapper;
import com.johanwork.warehouse.warehouse.dto.DetailWarehouseProductResponse;
import com.johanwork.warehouse.warehouse.dto.WarehouseProductRequest;
import com.johanwork.warehouse.warehouse.dto.WarehouseProductResponse;
import com.johanwork.warehouse.warehouse.dto.WarehouseResponse;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import com.johanwork.warehouse.warehouse.entity.WarehouseProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WarehouseProductMapper implements GenericResponseMapper<WarehouseProduct, WarehouseProductRequest, WarehouseProductResponse> {

    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public WarehouseProductResponse mapEntityToResponse(WarehouseProduct model) {
        return new WarehouseProductResponse(
                model.getId(),
                warehouseMapper.mapEntityToResponse(model.getWarehouse()),
                productMapper.mapEntityToResponse(model.getProduct()),
                model.getStock()
        );
    }

    @Override
    public WarehouseProduct mapRequestToEntity(WarehouseProductRequest request) {
        return null;
    }

    public WarehouseProduct mapRequestToEntity(WarehouseProductRequest request, Warehouse warehouse) {
        Product product = new Product();
        product.setId(request.productId());
        return new WarehouseProduct(
                null,
                warehouse,
                product,
                request.stock()
        );
    }

    @Override
    public List<WarehouseProductResponse> mapListEntityToListResponse(List<WarehouseProduct> m) {
        if (null != m) {
            return m.stream().map(this::mapEntityToResponse).toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<WarehouseProductResponse> mapPageEntityToPageResponse(Page<WarehouseProduct> m) {
        if (null != m) {
            return new PageResponse<>(
                    m.map(this::mapEntityToResponse).getContent(),
                    m.getNumber(),
                    m.getSize(),
                    m.getTotalElements(),
                    m.getTotalPages(),
                    m.hasNext(),
                    m.hasPrevious());
        }
        return new PageResponse<>();
    }

    public GenericResponse<DetailWarehouseProductResponse> mapToDetailWPGenericResponse(DetailWarehouseProductResponse detail, String message) {
        return new GenericResponse<>(detail, message);
    }
}
