package com.example.shop.service;

import com.example.shop.dto.CaptchaConfigDTO;
import com.example.shop.dto.CaptchaVerifyDTO;
import com.example.shop.entity.CaptchaConfig;
import java.util.Map;

public interface CaptchaService {
    
    CaptchaConfig getConfig();
    
    void updateConfig(CaptchaConfigDTO dto);
    
    boolean isEnabled();
    
    Map<String, Object> generateCaptcha(String ip);
    
    Map<String, Object> generateCaptcha(String ip, String type);
    
    boolean verifyCaptcha(CaptchaVerifyDTO dto, String ip);
    
    void checkIpLimit(String ip);
}
