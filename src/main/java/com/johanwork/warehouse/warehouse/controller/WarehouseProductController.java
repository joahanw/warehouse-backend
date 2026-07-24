package com.johanwork.warehouse.warehouse.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.warehouse.dto.*;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import com.johanwork.warehouse.warehouse.entity.WarehouseProduct;
import com.johanwork.warehouse.warehouse.service.IWarehouseProductService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouse-products")
public class WarehouseProductController {

    private final IWarehouseProductService warehouseProductService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<WarehouseProductResponse>>> getAllWarehouseProduct(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.getAllWarehouseProduct(pageNumber, pageSize, sortBy, sortDirection));
    }

    @GetMapping(path = "/{warehouseId}", version = "1.0")
    public ResponseEntity<GenericResponse<List<WarehouseProductResponse>>> getDetailWarehouseProductByWarehouseId(
            @PathVariable Long warehouseId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.getDetailWarehouseProductByWarehouseId(warehouseId));
    }

    @GetMapping(path = "/{warehouseId}/detail/{productId}", version = "1.0")
    public ResponseEntity<GenericResponse<WarehouseProductResponse>> getWarehouseProductByWarehouseIDAndProductID(
            @PathVariable long warehouseId,
            @PathVariable long productId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.getWarehouseProductByWarehouseIdAndProductId(warehouseId, productId));
    }

    @GetMapping(path = "/detail/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<WarehouseProductResponse>> getDetailWarehouseProductByID(@PathVariable long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.getDetailWarehouseProductById(id));
    }

    @GetMapping(path = "/detail/products/{productId}", version = "1.0")
    public ResponseEntity<GenericResponse<List<WarehouseResponse>>> getWarehouseProductByProductID(@PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.getWarehouseProductByProductId(productId));
    }

    @GetMapping(path = "/detail/products/{productId}/total-stock", version = "1.0")
    public ResponseEntity<GenericResponse<ProductTotalStockResponse>> getProductTotalStock(@PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.getProductTotalStock(productId));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> create(@RequestBody @Valid WarehouseProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.create(request));
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> update(@PathVariable long id, @RequestBody @Valid WarehouseProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.update(id, request));
    }

    @DeleteMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.delete(id));
    }

    @DeleteMapping(path = "/detail/products/{productId}")
    public ResponseEntity<GenericResponse<Void>> deleteAllWarehouseProductByProductId(@PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseProductService.deleteAllWarehouseProductByProductId(productId));
    }

}
