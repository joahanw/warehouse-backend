package com.johanwork.warehouse.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {
    private BigDecimal totalRevenue;
    private Long totalTransaction;
    private Long productSold;
}
