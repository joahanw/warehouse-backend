package com.johanwork.warehouse.transaction.spesification;

import com.johanwork.warehouse.transaction.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {
    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");

    private TransactionSpecification(){}

    public static Specification<Transaction> filter(
            String search,
            Long merchantId
    ){
        return filter(search, merchantId, null, null);
    }

    public static Specification<Transaction> filter(
            String search,
            Long merchantId,
            Integer month,
            Integer year
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

            if(null != month || null != year){
                int resolvedYear = (null != year) ? year : LocalDate.now(ZONE).getYear();
                LocalDate start = (null != month) ? LocalDate.of(resolvedYear, month, 1) : LocalDate.of(resolvedYear, 1, 1);
                LocalDate end = (null != month) ? start.plusMonths(1) : start.plusYears(1);
                Instant startDate = start.atStartOfDay(ZONE).toInstant();
                Instant endDate = end.atStartOfDay(ZONE).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
