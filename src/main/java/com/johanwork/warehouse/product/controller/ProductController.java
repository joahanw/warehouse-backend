package com.johanwork.warehouse.product.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.product.dto.ProductRequest;
import com.johanwork.warehouse.product.dto.ProductResponse;
import com.johanwork.warehouse.product.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.getAllProducts(pageNumber, pageSize, sortBy, sortDirection, search));
    }

    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.getProductById(id));
    }

    @GetMapping(path = "/barcode/{barcode}", version = "1.0")
    public ResponseEntity<GenericResponse<ProductResponse>> getProductByBarcode(@PathVariable String barcode) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.getProductByBarcode(barcode));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> createProduct(@RequestBody @Valid ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> updateProduct(@PathVariable Long id,
                                                               @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.update(id, request));
    }

    @DeleteMapping(path = "/{id}",version = "1.0")
    public ResponseEntity<GenericResponse<Void>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.delete(id));
    }

}
