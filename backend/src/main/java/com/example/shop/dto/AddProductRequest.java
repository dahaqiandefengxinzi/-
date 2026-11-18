package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AddProductRequest {
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    private String subtitle;
    
    private BigDecimal price;
    
    private Integer stock;
    
    private Long categoryId;
    
    private String mainImage;
    
    private String description;
    
    private List<String> images;
    
    private Integer status;
}
