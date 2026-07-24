package com.johanwork.warehouse.warehouse.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.warehouse.dto.*;

import java.util.List;

public interface IWarehouseProductService {
    GenericResponse<PageResponse<WarehouseProductResponse>> getAllWarehouseProduct(int pageNumber, int pageSize,
                                                                                   String sortBy, String sortDirection);

    // Get Warehouse_Product base on warehouse_id (FK)
    // pada turunan WarehouseProductResponse tidak perlu memapping WarehouseResponse!!
    GenericResponse<List<WarehouseProductResponse>> getDetailWarehouseProductByWarehouseId(Long warehouseId);

    // Get Warehouse_Product base on warehouse_product_id (PK)
    GenericResponse<WarehouseProductResponse>getDetailWarehouseProductById(Long warehouseProductId);

    // Get Warehouse_Product base on warehouse_id (FK) and product_id (FK)
    // Nyatanya kita hanya membutuhkan ID, warehouse_id, product_id dan stock!
    GenericResponse<WarehouseProductResponse> getWarehouseProductByWarehouseIdAndProductId(Long warehouseId, Long productId);

    // Get Warehouse base on product_id (FK) on Warehouse_Product
    // Mengambil daftar warehouse yang menyimpan suatu produk berdasarkan product_id.
    // Tidak perlu menampilkan STOCK!
    GenericResponse<List<WarehouseResponse>> getWarehouseProductByProductId(Long productId);

    // Get Product Total Stock on warehouse_product
    // Mengehitung dan total semua stock product pada tiap warehouse yang berbeda
    GenericResponse<ProductTotalStockResponse> getProductTotalStock(Long productId);

    GenericResponse<Void> create(WarehouseProductRequest request);
    GenericResponse<Void> update(Long id, WarehouseProductRequest request);
    GenericResponse<Void> delete(Long id);

    // Delete ALL Warehouse_Product base on product_id (FK)
    GenericResponse<Void> deleteAllWarehouseProductByProductId(Long productId);

}
