package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "地址ID不能为空")
    private Long addressId;
    
    @NotNull(message = "支付方式不能为空")
    private Integer paymentType;
    
    @NotNull(message = "购物车项不能为空")
    private List<Long> cartIds;
}
