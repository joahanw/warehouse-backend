package com.johanwork.warehouse.merchant.mapper;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantProductRequest;
import com.johanwork.warehouse.merchant.dto.MerchantProductResponse;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.product.mapper.ProductMapper;
import com.johanwork.warehouse.user.mapper.UserMapper;
import com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import com.johanwork.warehouse.warehouse.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MerchantProductMapper implements GenericResponseMapper<MerchantProduct, MerchantProductRequest, MerchantProductResponse> {

    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public MerchantProductResponse mapEntityToResponse(MerchantProduct merchantProduct) {
        return new MerchantProductResponse(
                merchantProduct.getId(),
                merchantMapper.mapEntityToResponse(merchantProduct.getMerchant()),
                productMapper.mapEntityToResponse(merchantProduct.getProduct()),
                warehouseMapper.mapEntityToResponse(merchantProduct.getWarehouse()),
                merchantProduct.getStock()
        );
    }

    @Override
    public MerchantProduct mapRequestToEntity(MerchantProductRequest merchantProductRequest) {
        return null;
    }

    public MerchantProduct mapRequestToEntity(Merchant merchant, Product product, Warehouse warehouse, Integer stock) {
        return new MerchantProduct(
                null,
                merchant,
                product,
                warehouse,
                stock
        );
    }

    @Override
    public List<MerchantProductResponse> mapListEntityToListResponse(List<MerchantProduct> m) {
        if (null != m){
            return m.stream().map(this::mapEntityToResponse).toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<MerchantProductResponse> mapPageEntityToPageResponse(Page<MerchantProduct> m) {
        if (null != m){
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

    public GenericResponse<ProductTotalStockResponse> mapToProductStockResponse(ProductTotalStockResponse response, String message) {
        return new GenericResponse<>(response, message);
    }
}
