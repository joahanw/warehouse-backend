package com.johanwork.warehouse.merchant.repository;

import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import com.johanwork.warehouse.warehouse.dto.ProductTotalStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Optional;

public interface MerchantProductRepository extends JpaRepository<MerchantProduct, Long> ,
        JpaSpecificationExecutor<MerchantProduct> {

    Page<MerchantProduct> findAll(
            Specification<MerchantProduct> spec,
            Pageable pageable
    );

    ProductTotalStockResponse getTotalStockByProductId(Long productId);
    void deleteMerchantProductByProduct_Id(Long productId);


    @EntityGraph(attributePaths = {
            "merchant",
            "product",
            "product.category",
            "warehouse"
    })
    Optional<MerchantProduct> findByProduct_IdAndMerchant_Id(Long productId, Long merchantId);

    @Modifying
    void reduceMerchantProductStock(@Param("merchantId") Long merchantId,
                                    @Param("productId") Long productId,
                                    @Param("quantity") Long quantity);

}
