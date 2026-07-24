package com.johanwork.warehouse.merchant.mapper;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantProductResponse;
import com.johanwork.warehouse.merchant.dto.MerchantRequest;
import com.johanwork.warehouse.merchant.dto.MerchantResponse;
import com.johanwork.warehouse.merchant.dto.MerchantWithMerchantProductResponse;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.product.mapper.ProductMapper;
import com.johanwork.warehouse.user.mapper.UserMapper;
import com.johanwork.warehouse.warehouse.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MerchantMapper implements GenericResponseMapper<Merchant, MerchantRequest, MerchantResponse> {

    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public MerchantResponse mapEntityToResponse(Merchant merchant) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getName(),
                merchant.getAddress(),
                merchant.getPhoto(),
                merchant.getPhone(),
                merchant.getKeeper().getId(),
                merchant.getKeeper().getName(),
                0L
        );
    }

    @Override
    public Merchant mapRequestToEntity(MerchantRequest request) {
        return new Merchant(
                null,
                request.name(),
                request.address(),
                request.photo(),
                request.phone(),
                false,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public List<MerchantResponse> mapListEntityToListResponse(List<Merchant> m) {
        if (null != m){
            return m.stream().map(this::mapEntityToResponse).toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<MerchantResponse> mapPageEntityToPageResponse(Page<Merchant> m) {
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

    public GenericResponse<PageResponse<MerchantWithMerchantProductResponse>> mapToMerchantWithMerchantProductResponse(Page<Merchant> merchants, String message) {
        if (null != merchants) {
            var merchantProductsRes = new ArrayList<MerchantWithMerchantProductResponse>();
            merchants.forEach(data -> {
                var merchant = new MerchantWithMerchantProductResponse();
                var merchantProducts = new ArrayList<MerchantProductResponse>();
                merchant.setId(data.getId());
                merchant.setName(data.getName());
                merchant.setAddress(data.getAddress());
                merchant.setPhoto(data.getPhoto());
                merchant.setPhone(data.getPhone());
                merchant.setKeeperId(data.getKeeper().getId());
                merchant.setKeeperName(data.getKeeper().getName());
                data.getMerchantProducts().forEach(mp -> {
                    var merchantProduct = new MerchantProductResponse();
                    merchantProduct.setId(mp.getId());
                    merchantProduct.setStock(mp.getStock());
                    merchantProduct.setProduct(productMapper.mapEntityToResponse(mp.getProduct()));
                    merchantProduct.setWarehouse(warehouseMapper.mapEntityToResponse(mp.getWarehouse()));
                    merchantProducts.add(merchantProduct);
                });
                merchant.setMerchantProducts(merchantProducts);
                merchantProductsRes.add(merchant);
            });
            var res = new PageResponse<>(
                    merchantProductsRes,
                    merchants.getNumber(),
                    merchants.getSize(),
                    merchants.getTotalElements(),
                    merchants.getTotalPages(),
                    merchants.hasNext(),
                    merchants.hasPrevious());
            return new GenericResponse<>(res, message);
        }
        return new GenericResponse<>(new PageResponse<>(), message);
    }

    public GenericResponse<MerchantResponse> mapToMerchantResponse(MerchantResponse res, String message){
        return new GenericResponse<>(res, message);
    }
}
