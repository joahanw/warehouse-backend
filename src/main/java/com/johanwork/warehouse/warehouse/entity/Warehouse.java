package com.johanwork.warehouse.warehouse.entity;

import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "warehouses")
@SoftDelete
@NamedQueries({
        @NamedQuery(name = "Warehouse.getWarehouseByProductId",
        query = """
        SELECT w
        FROM Warehouse w
        LEFT JOIN FETCH w.warehouseProducts wp
        WHERE wp.product.id = :productId
        """)
})
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String address;

    @Column(columnDefinition = "text")
    private String photo;

    @Column(nullable = false, length = 20)
    private String phone;

    @OneToMany(mappedBy = "warehouse")
    private Set<WarehouseProduct> warehouseProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "warehouse")
    private Set<MerchantProduct> merchantProducts = new LinkedHashSet<>();
}
