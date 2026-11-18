package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long productId;
    
    private Integer quantity;
    
    private Integer selected;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String productName;
    
    @TableField(exist = false)
    private String productImage;
    
    @TableField(exist = false)
    private BigDecimal price;
    
    @TableField(exist = false)
    private Integer stock;
}
