package com.johanwork.warehouse.category.service.impl;

import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.category.entity.Category;
import com.johanwork.warehouse.category.repository.CategoryRepository;
import com.johanwork.warehouse.category.service.ICategoryDomainService;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryDomainService implements ICategoryDomainService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories", key = "'id:' + #id")
    @Override
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "CATEGORY"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Category", id)));
    }

    @Cacheable(value = "category-response", key = "'id:' + #id")
    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.getCategoryById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "CATEGORY"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Category", id)));
    }

}
