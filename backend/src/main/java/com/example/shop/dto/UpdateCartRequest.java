package com.example.shop.dto;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Integer quantity;
    
    private Integer selected;
}
