package com.johanwork.warehouse.product.service.impl;

import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.product.repository.ProductRepository;
import com.johanwork.warehouse.product.service.IProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductDomainService implements IProductDomainService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "'id:' + #id")
    @Override
    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND,"Product"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND,"Product", id)
                ));
    }

    @Cacheable(value = "products", key = "'barcode:' + #barcode")
    @Override
    public Product findProductByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "Product"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Product", barcode)
                ));
    }
}
