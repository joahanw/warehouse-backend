package com.johanwork.warehouse.merchant.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.merchant.dto.MerchantRequest;
import com.johanwork.warehouse.merchant.dto.MerchantResponse;
import com.johanwork.warehouse.merchant.dto.MerchantWithMerchantProductResponse;
import com.johanwork.warehouse.merchant.service.impl.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<MerchantWithMerchantProductResponse>>> getAllMerchant(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long keeperId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantService.getAllMerchant(pageNumber, pageSize, sortBy, sortDirection, search, keeperId));
    }

    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<MerchantResponse>> getMerchantById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantService.getMerchantById(id));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> create(@RequestBody @Valid MerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantService.create(request));
    }

    @PutMapping(path = "/{id}",version = "1.0")
    public ResponseEntity<GenericResponse<Void>> update(@PathVariable Long id,
                                                        @RequestBody @Valid MerchantRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantService.update(id, request));
    }

    @DeleteMapping(path = "/{id}",version = "1.0")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantService.delete(id));
    }

}
