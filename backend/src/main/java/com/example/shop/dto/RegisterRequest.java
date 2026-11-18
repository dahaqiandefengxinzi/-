package com.example.shop.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegisterRequest {
    private String username;
    private String method;
    private String code;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "密码至少8位，包含字母和数字")
    private String password;
    
    private String email;
    
    private String phone;
}
