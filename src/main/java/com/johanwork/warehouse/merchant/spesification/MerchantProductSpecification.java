package com.johanwork.warehouse.merchant.spesification;

import com.johanwork.warehouse.merchant.entity.MerchantProduct;
import com.johanwork.warehouse.product.entity.Product;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class MerchantProductSpecification {

    private MerchantProductSpecification(){}

    public static Specification<MerchantProduct> filter(
            Integer stock,
            Long merchantId,
            Long productId
    ){
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(null != merchantId){
                predicates.add(
                        cb.equal(root.get("merchant").get("id"), merchantId)
                );
            }

            if(null != productId){
                predicates.add(
                        cb.equal(root.get("product").get("id"), productId)
                );
            }

            if (null != stock) {
                predicates.add(
                        cb.equal(root.get("stock"), stock)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
