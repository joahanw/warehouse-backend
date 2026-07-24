package com.johanwork.warehouse.merchant.spesification;

import com.johanwork.warehouse.merchant.entity.Merchant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MerchantSpecification {
    private MerchantSpecification(){}

    public static Specification<Merchant> filter(
            Long keeperId,
            String search
    ){
        return (root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();

            if(null != keeperId){
                predicates.add(cb.equal(root.get("keeper").get("id"), keeperId));
            }

            if(null != search){
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(root.get("name"), keyword),
                                cb.like(root.get("address"), keyword)
                        )
                );
            }
            predicates.add(cb.equal(root.get("deleted"),false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
