package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("`order`")
public class Order implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderSn;
    
    private Long userId;
    
    private BigDecimal totalAmount;
    
    private BigDecimal payAmount;
    
    private Integer status;
    
    private Integer paymentType;
    
    private String shippingAddress;
    
    private String shippingCompany;
    
    private String shippingNumber;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    private LocalDateTime payTime;
    
    private LocalDateTime deliveryTime;
    
    private LocalDateTime receiveTime;
    
    @TableField(exist = false)
    private List<OrderItem> orderItems;
}
