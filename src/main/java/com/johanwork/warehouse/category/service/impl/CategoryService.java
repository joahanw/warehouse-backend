package com.johanwork.warehouse.category.service.impl;

import com.johanwork.warehouse.category.dto.CategoryRequest;
import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.category.entity.Category;
import com.johanwork.warehouse.category.mapper.CategoryMapper;
import com.johanwork.warehouse.category.repository.CategoryRepository;
import com.johanwork.warehouse.category.service.ICategoryDomainService;
import com.johanwork.warehouse.category.service.ICategoryService;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService implements ICategoryService {

    private final ICategoryDomainService domainService;
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    @Cacheable(
            value = "category-list",
            condition = "#search == null || #search.isBlank()",
            key = "T(String).format('%d-%d-%s-%s', #pageNumber, #pageSize, #sortBy, #sortDirection)"
    )
    @Override
    public GenericResponse<PageResponse<CategoryResponse>> getAllCategories(int pageNumber, int pageSize,
                                                                            String sortBy, String sortDirection,
                                                                            String search) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<CategoryResponse> categories = null == search
                ? categoryRepository.getAllCategory(pageable)
                : categoryRepository.getAllCategoryByNameOrTagline(search, pageable);
        return categoryMapper.mapToPageCategoryResponseToPageResponse(categories,
                String.format(AppConstant.Success.FETCHED,"Category"));
    }

    @Override
    public GenericResponse<CategoryResponse> getCategoryById(Long id) {
        return categoryMapper.mapToGenericResponse(domainService.getCategoryById(id),
                String.format(AppConstant.Success.FETCHED, "Category"));
    }

    @Caching(evict = {
            @CacheEvict(value = "category-list", allEntries = true),
            @CacheEvict(value = "categories", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> create(CategoryRequest categoryRequest) {
        Category category = categoryMapper.mapRequestToEntity(categoryRequest);
        categoryRepository.save(category);
        return categoryMapper.mapToGenericResponse(String.format(AppConstant.Success.CREATED, "Category"));
    }

    @Caching(evict = {
            @CacheEvict(value = "category-list", allEntries = true),
            @CacheEvict(value = "categories", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> update(Long id, CategoryRequest categoryRequest) {
        domainService.findCategoryById(id);
        Category category = categoryMapper.mapRequestToEntity(categoryRequest);
        category.setId(id);
        categoryRepository.save(category);
        return categoryMapper.mapToGenericResponse(String.format(AppConstant.Success.UPDATED, "Category"));
    }

    @Caching(evict = {
            @CacheEvict(value = "category-list", allEntries = true),
            @CacheEvict(value = "categories", allEntries = true)
    })
    @Transactional
    @Override
    public GenericResponse<Void> delete(Long id) {
        categoryRepository.delete(domainService.findCategoryById(id));
        return categoryMapper.mapToGenericResponse(String.format(AppConstant.Success.DELETED, "Category"));
    }


}
