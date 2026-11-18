package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String subtitle;
    
    private BigDecimal price;
    
    private Integer stock;
    
    private Long categoryId;
    
    private Long brandId;
    
    private Integer status;
    
    private Integer sales;
    
    private String mainImage;
    
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
