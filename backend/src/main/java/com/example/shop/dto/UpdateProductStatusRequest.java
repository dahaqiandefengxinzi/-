package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class UpdateProductStatusRequest {
    @NotNull(message = "商品状态不能为空")
    private Integer status;
}
