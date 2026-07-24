package com.johanwork.warehouse.merchant.entity;

import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.transaction.entity.Transaction;
import com.johanwork.warehouse.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "merchants")
@SQLDelete(sql = """
    UPDATE merchants SET deleted = true WHERE id = ?
""")
@NamedQueries({
        @NamedQuery(name = "Merchant.getMerchantById",
                query = """
                SELECT new com.johanwork.warehouse.merchant.dto.MerchantResponse(
                    m.id,
                    m.name,
                    m.address,
                    m.photo,
                    m.phone,
                    m.keeper.id,
                    m.keeper.name,
                    COUNT(mp.id)
                )
                FROM Merchant m
                LEFT JOIN m.merchantProducts mp
                WHERE m.id = :id and m.deleted = false
                GROUP BY m.id, m.name, m.address, m.photo, m.phone, m.keeper.id, m.keeper.name
                """),
})
public class Merchant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String address;

    private String photo;

    private String phone;

    private boolean deleted = false;

    private String code;
    private String telegramChatId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "keeper_id", nullable = false, unique = true)
    private User keeper;

    @OneToMany(mappedBy = "merchant")
    private Set<MerchantProduct> merchantProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "merchant")
    private List<Transaction> transactions = new ArrayList<>();
}

