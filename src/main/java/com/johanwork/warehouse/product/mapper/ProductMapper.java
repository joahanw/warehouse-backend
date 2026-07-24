package com.johanwork.warehouse.product.mapper;

import com.johanwork.warehouse.category.mapper.CategoryMapper;
import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.product.dto.ProductRequest;
import com.johanwork.warehouse.product.dto.ProductResponse;
import com.johanwork.warehouse.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper implements GenericResponseMapper<Product, ProductRequest, ProductResponse> {

    private final CategoryMapper categoryMapper;

    @Override
    public ProductResponse mapEntityToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBarcode(),
                product.getPrice(),
                product.getAbout(),
                product.getThumbnail(),
                product.getIsPopular(),
                categoryMapper.mapEntityToResponse(product.getCategory())
        );
    }

    @Override
    public Product mapRequestToEntity(ProductRequest productRequest) {
        return new Product(
                null,
                productRequest.name(),
                productRequest.barcode(),
                productRequest.thumbnail(),
                productRequest.about(),
                productRequest.price(),
                productRequest.isPopular(),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public List<ProductResponse> mapListEntityToListResponse(List<Product> m) {
        if(null != m){
            return m.stream()
                    .map(this::mapEntityToResponse).toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<ProductResponse> mapPageEntityToPageResponse(Page<Product> m) {
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
}
