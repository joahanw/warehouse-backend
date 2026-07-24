package com.johanwork.warehouse.warehouse.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.warehouse.dto.WarehouseRequest;
import com.johanwork.warehouse.warehouse.dto.WarehouseResponse;
import com.johanwork.warehouse.warehouse.service.IWarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/warehouses")
public class WarehouseController {

    private final IWarehouseService warehouseService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<WarehouseResponse>>> getAllWarehouse(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseService.getAllWarehouse(pageNumber, pageSize, sortBy, sortDirection, search));
    }

    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<WarehouseResponse>> getById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseService.getWarehouseById(id));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> create(@RequestBody @Valid WarehouseRequest warehouseRequest){
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseService.createWarehouse(warehouseRequest));
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> update(@PathVariable Long id,
                                                        @RequestBody WarehouseRequest warehouseRequest){
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseService.updateWarehouse(id, warehouseRequest));
    }

    @DeleteMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(warehouseService.deleteWarehouse(id));
    }

}
