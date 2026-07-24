package com.johanwork.warehouse.transaction.repository;

import com.johanwork.warehouse.transaction.dto.response.DashboardStatsByMerchantResponse;
import com.johanwork.warehouse.transaction.dto.response.DashboardStatsResponse;
import com.johanwork.warehouse.transaction.entity.Transaction;
import com.johanwork.warehouse.transaction.spesification.TransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository  extends JpaRepository<Transaction,Long> {
   DashboardStatsByMerchantResponse getDashboardStats();
   DashboardStatsByMerchantResponse getDashboardStatsByMerchant(@Param("merchantId") Long merchantId);

   @EntityGraph(attributePaths = {
           "merchant",
           "transactionProducts",
           "transactionProducts.product"
   })
   Optional<Transaction> findByOrderId(String orderId);

   @EntityGraph(attributePaths = {
           "merchant",
           "transactionProducts",
           "transactionProducts.product",
           "transactionProducts.product.category"
   })
   Page<Transaction> findAll(
           Specification<Transaction> spec,
           Pageable pageable
   );
}
