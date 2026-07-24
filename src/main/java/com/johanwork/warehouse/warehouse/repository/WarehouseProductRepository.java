package com.johanwork.warehouse.warehouse.repository;

import com.johanwork.warehouse.warehouse.dto.DetailWarehouseProductResponse;
import com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse;
import com.johanwork.warehouse.warehouse.entity.WarehouseProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, Long> {

    @EntityGraph(attributePaths = {
            "warehouse",
            "product"
    })
    Page<WarehouseProduct> findAll(Pageable pageable);

    List<WarehouseProduct> getDetailByWarehouseId(@Param("warehouseId") Long warehouseId);
    Optional<WarehouseProduct> getDetailById(Long id);
    Optional<WarehouseProduct> getWarehouseProductByWarehouseIdAndProductId(@Param("warehouseId") Long warehouseId,
                                                                            @Param("productId") Long productId);
    ProductTotalStockResponse getTotalStockByProductId(@Param("productId") Long productId);

    @Modifying
    void updateProductOnlyStock(@Param("stock") int stock, @Param("updateBy") String updateBy,
                                @Param("warehouseId") Long warehouseId, @Param("id") Long id);

    @Modifying
    void updateProductId(@Param("stock") int stock, @Param("updateBy") String updateBy,
                         @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
                         @Param("id") Long id);

    boolean existsByWarehouseIdAndStockGreaterThan(Long warehouseId, int stock);

    void deleteWarehouseProductByProduct_Id(Long productId);

    boolean existsByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
