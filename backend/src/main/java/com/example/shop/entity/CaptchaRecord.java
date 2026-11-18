package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("captcha_record")
public class CaptchaRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String captchaId;
    
    private String captchaType;
    
    private String captchaKey;
    
    private Long userId;
    
    private String ip;
    
    private Integer retryCount;
    
    private Integer status;
    
    private LocalDateTime expireTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
