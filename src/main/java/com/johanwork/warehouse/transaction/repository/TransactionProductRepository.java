package com.johanwork.warehouse.transaction.repository;

import com.johanwork.warehouse.transaction.entity.Transaction;
import com.johanwork.warehouse.transaction.entity.TransactionProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionProductRepository extends JpaRepository<TransactionProduct,Long> {

    @EntityGraph(attributePaths = {
            "transaction",
            "product",
            "product.category"
    })
    Page<TransactionProduct> findAll(
            Specification<TransactionProduct> spec,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "transaction",
            "product"
    })
    Optional<TransactionProduct> findByTransaction_Id(Long transactionId);

}
