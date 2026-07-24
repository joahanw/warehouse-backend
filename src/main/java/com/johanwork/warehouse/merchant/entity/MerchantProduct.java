package com.johanwork.warehouse.merchant.entity;

import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.warehouse.entity.Warehouse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SoftDelete;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "merchant_products")
@SoftDelete
@NamedQueries({
    @NamedQuery(name = "MerchantProduct.getTotalStockByProductId",
            query = """
            SELECT new com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse(
                mp.product.id,
                SUM(mp.stock)
            )
            FROM MerchantProduct mp
            LEFT JOIN mp.product p
            WHERE mp.product.id = :productId
            GROUP BY mp.product.id
            """),
        @NamedQuery(name = "MerchantProduct.reduceMerchantProductStock",
        query = """
        UPDATE MerchantProduct mp
        SET mp.stock = mp.stock - :quantity,
            mp.updatedAt = CURRENT_TIMESTAMP,
            mp.updatedBy = "SYSTEM"
        WHERE mp.product.id = :productId
        AND mp.merchant.id = :merchantId
        """)
})
public class MerchantProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull
    @Column(nullable = false)
    private Integer stock;
}
