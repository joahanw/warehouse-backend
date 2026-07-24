package com.johanwork.warehouse.category.service;

import com.johanwork.warehouse.category.dto.CategoryRequest;
import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;

public interface ICategoryService {
    GenericResponse<PageResponse<CategoryResponse>> getAllCategories(int pageNumber, int pageSize, String sortBy, String sortDirection, String search);
    GenericResponse<CategoryResponse> getCategoryById(Long id);
    GenericResponse<Void> create(CategoryRequest categoryRequest);
    GenericResponse<Void> update(Long id, CategoryRequest categoryRequest);
    GenericResponse<Void> delete(Long id);
}
