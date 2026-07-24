package com.johanwork.warehouse.category.entity;


import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categories")
@NamedQueries({
        @NamedQuery(name = "Category.getCategoryById",
                query = """
                     SELECT new com.johanwork.warehouse.category.dto.CategoryResponse(
                c.id,
                c.name,
                c.tagline,
                c.photo,
                COUNT(p.id)
            )
            FROM Category c
            LEFT JOIN c.products p
            WHERE c.id = :id
            GROUP BY c.id, c.name, c.tagline, c.photo
            """)
})
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String tagline;

    private String photo;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
}
