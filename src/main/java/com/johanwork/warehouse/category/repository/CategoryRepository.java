package com.johanwork.warehouse.category.repository;

import com.johanwork.warehouse.category.dto.CategoryResponse;
import com.johanwork.warehouse.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
     SELECT new com.johanwork.warehouse.category.dto.CategoryResponse(
            c.id,
            c.name,
            c.tagline,
            c.photo,
            COUNT(p.id)
        )
        FROM Category c
        LEFT JOIN c.products p
           WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.tagline) LIKE LOWER(CONCAT('%', :search, '%'))
        GROUP BY c.id, c.name, c.tagline, c.photo
     """)
    Page<CategoryResponse> getAllCategoryByNameOrTagline(@Param("search") String search, Pageable pageable);

    @Query("""
     SELECT new com.johanwork.warehouse.category.dto.CategoryResponse(
            c.id,
            c.name,
            c.tagline,
            c.photo,
            COUNT(p.id)
        )
        FROM Category c
        LEFT JOIN c.products p
        GROUP BY c.id, c.name, c.tagline, c.photo
    """)
    Page<CategoryResponse> getAllCategory(Pageable pageable);

    Optional<CategoryResponse> getCategoryById(Long id);
}
