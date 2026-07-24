package com.johanwork.warehouse.product.service;

import com.johanwork.warehouse.product.entity.Product;

public interface IProductDomainService {
    Product findProductById(Long id);
    Product findProductByBarcode(String barcode);
}
