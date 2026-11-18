package com.example.shop.dto;

import lombok.Data;

@Data
public class CaptchaConfigDTO {
    private Boolean enabled;
    private String captchaType;
    private Integer expireTime;
    private Integer maxRetryCount;
    private Integer lockTime;
    private Integer refreshInterval;
    private Integer ipLimitCount;
}
