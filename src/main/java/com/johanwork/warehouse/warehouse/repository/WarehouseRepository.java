package com.johanwork.warehouse.warehouse.repository;

import com.johanwork.warehouse.warehouse.dto.WarehouseResponse;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    @Query("""
         SELECT new com.johanwork.warehouse.warehouse.dto.WarehouseResponse(
            w.id,
            w.name,
            w.address,
            w.photo,
            w.phone,
            COUNT(wp.product)
        )
        FROM Warehouse w
        LEFT JOIN w.warehouseProducts wp
        LEFT JOIN wp.product p
        GROUP BY w.id, w.name, w.address, w.photo, w.phone
    """)
    Page<WarehouseResponse> getAllWarehouses(Pageable pageable);

    @Query("""
        SELECT new com.johanwork.warehouse.warehouse.dto.WarehouseResponse(
            w.id,
            w.name,
            w.address,
            w.photo,
            w.phone,
            COUNT(wp.product)
        )
        FROM Warehouse w
        LEFT JOIN w.warehouseProducts wp
        LEFT JOIN wp.product p
        WHERE (LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(w.address) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(w.phone) LIKE LOWER(CONCAT('%', :search, '%')))
        GROUP BY w.id, w.name, w.address, w.photo, w.phone
    """)
    Page<WarehouseResponse> getAllWarehousesByNameOrAddressOrPhone(String search, Pageable pageable);

    List<Warehouse> getWarehouseByProductId(Long productId);
}
