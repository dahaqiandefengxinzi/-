package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;
    
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String avatar;
    
    private Integer gender;
    
    private String birthday;
    
    private Integer status;
    
    private Integer role;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String token;
}
