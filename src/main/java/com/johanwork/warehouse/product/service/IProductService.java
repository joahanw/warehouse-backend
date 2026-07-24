package com.johanwork.warehouse.product.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.product.dto.ProductRequest;
import com.johanwork.warehouse.product.dto.ProductResponse;

public interface IProductService {
    GenericResponse<PageResponse<ProductResponse>> getAllProducts(int pageNumber, int pageSize,
                                                                  String sortBy, String sortDirection, String search);
    GenericResponse<ProductResponse> getProductById(Long id);
    GenericResponse<ProductResponse> getProductByBarcode(String barcode);
    GenericResponse<Void> create(ProductRequest product);
    GenericResponse<Void> update(Long id, ProductRequest product);
    GenericResponse<Void> delete(Long id);
}
