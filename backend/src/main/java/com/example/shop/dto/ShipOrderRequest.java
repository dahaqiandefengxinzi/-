package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ShipOrderRequest {
    @NotBlank(message = "物流公司不能为空")
    private String shippingCompany;
    
    @NotBlank(message = "物流单号不能为空")
    private String shippingNumber;
}
