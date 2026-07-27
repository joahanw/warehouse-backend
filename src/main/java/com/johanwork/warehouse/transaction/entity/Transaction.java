package com.johanwork.warehouse.transaction.entity;

import com.johanwork.warehouse.common.audit.BaseEntity;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.transaction.dto.FraudStatus;
import com.johanwork.warehouse.transaction.dto.PaymentMethod;
import com.johanwork.warehouse.transaction.dto.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.validator.constraints.pl.NIP;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactions")
@SoftDelete
@NamedQueries({
    @NamedQuery(
        name = "Transaction.getDashboardStats",
        query = """
        SELECT new com.johanwork.warehouse.transaction.dto.response.DashboardStatsByMerchantResponse(
             COALESCE(SUM(t.grandTotal), 0),
            COUNT(t.id),
           (SELECT COALESCE(SUM(tp.quantity), 0)
             FROM TransactionProduct tp JOIN tp.transaction t
             WHERE t.paymentStatus='success'),
             1L,
             "0"
        )FROM Transaction t
        WHERE t.paymentStatus = 'success'
        """
    ),
    @NamedQuery(
            name = "Transaction.getDashboardStatsByMerchant",
            query = """
    SELECT new com.johanwork.warehouse.transaction.dto.response.DashboardStatsByMerchantResponse(
        COALESCE(SUM(t.grandTotal), 0),
        COUNT(t.id),
        (SELECT COALESCE(SUM(tp.quantity), 0)
             FROM TransactionProduct tp JOIN tp.transaction t
             WHERE t.paymentStatus='success'),
        t.merchant.id,
        t.merchant.name
    )FROM Transaction t
    WHERE t.paymentStatus = 'success' and t.merchant.id = :merchantId
    GROUP BY t.merchant.id, t.merchant.name
    """
    )
})
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String name;

    @Size(max = 15)
    @Column(length = 15)
    private String phone;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal subTotal;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    // Midtrans Requirements
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String paymentCode;

    @Column(unique = true)
    private String orderId;
    private String transactionCode;

    @Column(columnDefinition = "TEXT")
    private String paymentToken;

    @Column(columnDefinition = "TEXT")
    private String callbackUrl;
    private Instant expiredAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
    private String currency;

    @Enumerated(EnumType.STRING)
    private FraudStatus fraudStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @OneToMany(mappedBy = "transaction")
    private Set<TransactionProduct> transactionProducts = new LinkedHashSet<>();

}
