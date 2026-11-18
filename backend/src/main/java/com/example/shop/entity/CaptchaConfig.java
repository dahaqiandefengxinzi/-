package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("captcha_config")
public class CaptchaConfig implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("enabled")
    private Integer enabled;
    
    private String captchaType;
    
    private Integer expireTime;
    
    private Integer maxRetryCount;
    
    private Integer lockTime;
    
    private Integer refreshInterval;
    
    private Integer ipLimitCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public boolean isCaptchaEnabled() {
        return enabled != null && enabled == 1;
    }
}
