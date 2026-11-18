package com.example.shop.dto;

import lombok.Data;
import java.util.List;

@Data
public class CaptchaVerifyDTO {
    private String captchaId;
    private String captchaType;
    private String captchaCode;
    private Integer sliderX;
    private List<List<Integer>> clickPoints;
}
