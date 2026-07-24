package com.johanwork.warehouse.product.entity;

import com.johanwork.warehouse.category.entity.Category;
import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import com.johanwork.warehouse.transaction.entity.TransactionProduct;
import com.johanwork.warehouse.warehouse.entity.WarehouseProduct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
@SoftDelete
@NamedQueries({
        @NamedQuery(name = "Product.getProductByNameOrBarcodeOrAbout",
        query = """
             SELECT p
             FROM Product p
             LEFT JOIN FETCH p.category
             WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.about) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String barcode;

    private String thumbnail;

    @Column(columnDefinition = "TEXT")
    private String about;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ColumnDefault("'false'")
    private Boolean isPopular = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product")
    private Set<WarehouseProduct> warehouseProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<MerchantProduct> merchantProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<TransactionProduct> transactionProducts = new LinkedHashSet<>();
}
