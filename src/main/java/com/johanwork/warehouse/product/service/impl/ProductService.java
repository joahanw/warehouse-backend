package com.johanwork.warehouse.product.service.impl;

import com.johanwork.warehouse.category.service.ICategoryDomainService;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.product.dto.ProductRequest;
import com.johanwork.warehouse.product.dto.ProductResponse;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.product.mapper.ProductMapper;
import com.johanwork.warehouse.product.repository.ProductRepository;
import com.johanwork.warehouse.product.service.IProductDomainService;
import com.johanwork.warehouse.product.service.IProductService;
import com.johanwork.warehouse.user.repository.UserRepository;
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

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ICategoryDomainService categoryService;
    private final IProductDomainService domainService;

    @Cacheable(
            value = "product-list",
            condition = "#search == null || #search.isBlank()",
            key = "T(String).format('%d-%d-%s-%s', #pageNumber, #pageSize, #sortBy, #sortDirection)"
    )
    @Override
    public GenericResponse<PageResponse<ProductResponse>> getAllProducts(int pageNumber, int pageSize,
                                                                         String sortBy, String sortDirection,
                                                                         String search) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> products = null == search
                ? productRepository.findAll(pageable)
                : productRepository.getProductByNameOrBarcodeOrAbout(search, pageable);
        return productMapper.mapToPageGenericResponse(products,
                String.format(AppConstant.Success.FETCHED,"Products"));
    }

    @Override
    public GenericResponse<ProductResponse> getProductById(Long id) {
        return productMapper.mapToGenericResponse(domainService.findProductById(id),
                String.format(AppConstant.Success.FETCHED,"Product"));
    }

    @Override
    public GenericResponse<ProductResponse> getProductByBarcode(String barcode) {
        return productMapper.mapToGenericResponse(domainService.findProductByBarcode(barcode),
                String.format(AppConstant.Success.FETCHED,"Product"));
    }

    @Caching(evict = {
            @CacheEvict(value = "product-list", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> create(ProductRequest request) {
        if (productRepository.findByBarcode(request.barcode()).isPresent()){
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    AppConstant.Error.TITLE_DUPLICATE,
                    String.format(AppConstant.Error.MESSAGE_DUPLICATE,"Product", request.barcode()));
        }
        Product product = productMapper.mapRequestToEntity(request);
        product.setCategory(categoryService.findCategoryById(request.categoryId()));
        productRepository.save(product);
        return productMapper.mapToGenericResponse(
                String.format(AppConstant.Success.CREATED,"Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "product-list", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> update(Long id, ProductRequest request) {
        Product oldData = domainService.findProductById(id);
        Product product = productMapper.mapRequestToEntity(request);
        product.setCategory(oldData.getCategory());
        product.setId(id);
        productRepository.save(product);
        return productMapper.mapToGenericResponse(
                String.format(AppConstant.Success.UPDATED,"Product")
        );
    }

    @Caching(evict = {
            @CacheEvict(value = "product-list", allEntries = true),
            @CacheEvict(value = "products", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> delete(Long id) {
        productRepository.delete(domainService.findProductById(id));
        return productMapper.mapToGenericResponse(
                String.format(AppConstant.Success.DELETED,"Product")
        );
    }

}
