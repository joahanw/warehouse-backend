package com.johanwork.warehouse.category.controller;

import com.johanwork.warehouse.category.dto.CategoryRequest;
import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.category.service.ICategoryService;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String search) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortDirection, search));
    }

    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<CategoryResponse>> getCategoryById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.getCategoryById(id));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<Void>> create(@RequestBody @Valid CategoryRequest categoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(categoryRequest));
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> update(@PathVariable Long id,
                                                        @RequestBody @Valid CategoryRequest categoryRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.update(id, categoryRequest));
    }

    @DeleteMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.delete(id));
    }

}
