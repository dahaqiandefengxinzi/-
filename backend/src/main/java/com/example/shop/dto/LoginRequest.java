package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String captchaId;
    
    private String captchaCode;
    
    private Integer sliderX;
    
    private List<ClickPoint> clickPoints;
    
    @Data
    public static class ClickPoint {
        private Integer x;
        private Integer y;
    }
}
