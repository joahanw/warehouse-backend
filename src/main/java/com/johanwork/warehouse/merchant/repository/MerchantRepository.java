package com.johanwork.warehouse.merchant.repository;

import com.johanwork.warehouse.merchant.dto.MerchantResponse;
import com.johanwork.warehouse.merchant.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    @EntityGraph(attributePaths = {
            "keeper",
            "merchantProducts",
            "merchantProducts.product",
            "merchantProducts.product.category",
            "merchantProducts.warehouse"
    })
    Page<Merchant> findAll(Specification<Merchant> spec, Pageable pageable);

    Optional<MerchantResponse> getMerchantById(Long id);
    Optional<Merchant> findByCode(String code);
    Optional<Merchant> findByKeeper_Id(Long userId);
}
