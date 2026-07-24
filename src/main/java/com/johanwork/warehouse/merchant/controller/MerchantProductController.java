package com.johanwork.warehouse.merchant.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantProductRequest;
import com.johanwork.warehouse.merchant.dto.MerchantProductResponse;
import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import com.johanwork.warehouse.merchant.service.IMerchantProductService;
import com.johanwork.warehouse.merchant.service.impl.MerchantProductService;
import com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/merchant-products")
public class MerchantProductController {

    private final IMerchantProductService merchantProductService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<MerchantProductResponse>>> getMerchantProducts(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantProductService.getMerchantProducts(pageNumber, pageSize, sortBy, sortDirection, stock, merchantId, productId));
    }

    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<MerchantProductResponse>> getMerchantProductById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantProductService.getMerchantProductById(id));
    }

    @GetMapping(path = "/barcode/{barcode}/merchant/{merchantId}", version = "1.0")
    public ResponseEntity<GenericResponse<MerchantProductResponse>> getMerchantProductByBarcodeAndMerchantId(@PathVariable String barcode,
                                                                                                             @PathVariable Long merchantId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantProductService.getMerchantProductByBarcode(merchantId, barcode));
    }

    @GetMapping(path = "/product/{productId}/total-stock", version = "1.0")
    public ResponseEntity<GenericResponse<ProductTotalStockResponse>> getMerchantProductTotalStockByProductId(@PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantProductService.getTotalStock(productId));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> create(@RequestBody @Valid MerchantProductRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantProductService.create(request));
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> update(@PathVariable Long id,
                                                        @RequestBody @Valid MerchantProductRequest request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantProductService.update(id, request));
    }

    @DeleteMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantProductService.delete(id));
    }

    @DeleteMapping(path = "/porduct/{productId}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> deleteAllByProductId(@PathVariable Long productId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantProductService.deleteAllByProductId(productId));
    }

}
