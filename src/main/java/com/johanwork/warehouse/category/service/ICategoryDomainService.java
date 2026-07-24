package com.johanwork.warehouse.category.service;

import com.johanwork.warehouse.category.dto.CategoryRequest;
import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.category.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ICategoryDomainService {
    Category findCategoryById(Long id);
    CategoryResponse getCategoryById(Long id);
}
