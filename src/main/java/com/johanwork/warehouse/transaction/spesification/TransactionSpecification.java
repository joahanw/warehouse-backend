package com.johanwork.warehouse.transaction.spesification;

import com.johanwork.warehouse.transaction.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {
    private TransactionSpecification(){}

    public static Specification<Transaction> filter(
            String search,
            Long merchantId
    ){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(null != merchantId){
                predicates.add(cb.equal(root.get("merchant").get("id"), merchantId));
            }

            if(null != search){
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(
                        cb.or(
                            cb.like(root.get("name"), keyword),
                            cb.like(root.get("phone"), keyword),
                            cb.like(root.get("email"), keyword)
                        )
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
