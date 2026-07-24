package com.johanwork.warehouse.transaction.spesification;

import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.transaction.entity.Transaction;
import com.johanwork.warehouse.transaction.entity.TransactionProduct;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionProductSpecification {
    private TransactionProductSpecification(){}

    public static Specification<TransactionProduct> filter(
            String search,
            Long merchantId
    ){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(null != merchantId){
                predicates.add(cb.equal(root.get("merchant").get("id"), merchantId));
            }

            if(null != search){
                Join<TransactionProduct, Transaction> transaction = root.join("transaction");
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(
                    cb.or(
                        cb.like(transaction.get("name"), keyword),
                        cb.like(transaction.get("phone"), keyword),
                        cb.like(transaction.get("email"), keyword)
                    )
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
