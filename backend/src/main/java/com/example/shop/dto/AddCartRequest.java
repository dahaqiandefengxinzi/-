package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AddCartRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    @NotNull(message = "数量不能为空")
    private Integer quantity;
}
