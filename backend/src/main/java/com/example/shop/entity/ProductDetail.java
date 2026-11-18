package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("product_detail")
public class ProductDetail implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private String description;
    
    private String specs;
    
    private String images;
}
