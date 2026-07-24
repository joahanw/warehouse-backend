package com.johanwork.warehouse.merchant.dto;

import com.johanwork.warehouse.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MerchantResponse {
    private Long id;
    private String name;
    private String address;
    private String photo;
    private String phone;
    private Long keeperId;
    private String keeperName;
    private Long productCount;
}
