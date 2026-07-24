package com.johanwork.warehouse.product.repository;

import com.johanwork.warehouse.product.dto.ProductResponse;
import com.johanwork.warehouse.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);
    Page<Product> getProductByNameOrBarcodeOrAbout(String search, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);

}
