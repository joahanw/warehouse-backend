package com.johanwork.warehouse.category.mapper;

import com.johanwork.warehouse.category.dto.CategoryRequest;
import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.category.entity.Category;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.GenericResponseMapper;
import com.johanwork.warehouse.common.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper implements GenericResponseMapper<Category, CategoryRequest, CategoryResponse> {

    @Override
    public CategoryResponse mapEntityToResponse(Category category) {
       return new CategoryResponse(
               category.getId(),
               category.getName(),
               category.getTagline(),
               category.getPhoto(),
               0L
       );
    }

    @Override
    public Category mapRequestToEntity(CategoryRequest categoryRequest) {
        return new Category(
                null,
                categoryRequest.name(),
                categoryRequest.tagline(),
                categoryRequest.photo(),
                null
        );
    }

    @Override
    public List<CategoryResponse> mapListEntityToListResponse(List<Category> m) {
        if(null != m){
            return m.stream()
                    .map(this::mapEntityToResponse).toList();
        }
        return List.of();
    }

    @Override
    public PageResponse<CategoryResponse> mapPageEntityToPageResponse(Page<Category> m) {
        if (null != m){
            return new PageResponse<>(
                    m.map(this::mapEntityToResponse).getContent(),
                    m.getNumber(),
                    m.getSize(),
                    m.getTotalElements(),
                    m.getTotalPages(),
                    m.hasNext(),
                    m.hasPrevious()
            );
        }
        return new PageResponse<>();
    }

    public GenericResponse<PageResponse<CategoryResponse>> mapToPageCategoryResponseToPageResponse(Page<CategoryResponse> m, String message) {
        if (null != m){
            var res = new PageResponse<>(
                    m.getContent(),
                    m.getNumber(),
                    m.getSize(),
                    m.getTotalElements(),
                    m.getTotalPages(),
                    m.hasNext(),
                    m.hasPrevious());
            return new GenericResponse<>(res, message);
        }
        return new GenericResponse<>( new PageResponse<>(), message);
    }

    public GenericResponse<CategoryResponse> mapToGenericResponse(CategoryResponse res, String message) {
        return new GenericResponse<>(res, message);
    }
}
