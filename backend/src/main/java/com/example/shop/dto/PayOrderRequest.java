package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PayOrderRequest {
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;
}
