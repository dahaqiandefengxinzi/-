package com.example.shop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailDTO {
    private Long id;
    private String name;
    private String subtitle;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String mainImage;
    private String description;
    private List<String> images;
    private Object specs;
    private Integer status;
}
