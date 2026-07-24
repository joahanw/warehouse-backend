package com.johanwork.warehouse.warehouse.entity;

import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "warehouse_products")
@NamedQueries({
        @NamedQuery(
        name = "WarehouseProduct.getDetailByWarehouseId",
        query = """
        SELECT wp
          FROM WarehouseProduct wp
          JOIN FETCH wp.warehouse w
          JOIN FETCH wp.product p
          JOIN FETCH p.category
        WHERE w.id = :warehouseId
        """
        ),
        @NamedQuery(
                name = "WarehouseProduct.getDetailById",
                query = """
         SELECT wp
          FROM WarehouseProduct wp
          JOIN FETCH wp.warehouse w
          JOIN FETCH wp.product p
          JOIN FETCH p.category
        WHERE wp.id = :id
        """
        ),
        @NamedQuery(
                name = "WarehouseProduct.getWarehouseProductByWarehouseIdAndProductId",
                query = """
         SELECT wp
          FROM WarehouseProduct wp
          JOIN FETCH wp.warehouse w
          JOIN FETCH wp.product p
          JOIN FETCH p.category
        WHERE w.id = :warehouseId
        AND p.id = :productId
        """
        ),
        @NamedQuery(
            name = "WarehouseProduct.getTotalStockByProductId",
            query = """
            SELECT new com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse(
                p.id,
                SUM(wp.stock)
            )
            FROM WarehouseProduct wp
            LEFT JOIN wp.product p
            WHERE p.id = :productId
            GROUP BY p.id
            """
        ),
        @NamedQuery(
            name = "WarehouseProduct.updateProductId",
            query = """
            UPDATE WarehouseProduct wp
            SET wp.stock = :stock,
                wp.product.id = :productId,
                wp.updatedBy = :updateBy,
                wp.updatedAt = CURRENT_TIMESTAMP
            WHERE wp.warehouse.id = :warehouseId
            and wp.id = :id
            """
        ),
        @NamedQuery(
                name = "WarehouseProduct.updateProductOnlyStock",
                query = """
            UPDATE WarehouseProduct wp
            SET wp.stock = :stock,
                wp.updatedBy = :updateBy,
                wp.updatedAt = CURRENT_TIMESTAMP
            WHERE wp.warehouse.id = :warehouseId
            and wp.id = :id
            """
        )
})

public class WarehouseProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(nullable = false)
    private Integer stock;

}
