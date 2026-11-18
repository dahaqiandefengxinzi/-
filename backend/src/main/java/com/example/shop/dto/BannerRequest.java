package com.example.shop.dto;

import lombok.Data;

@Data
public class BannerRequest {
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private Integer sort;
    private Integer status;
    private Integer intervalTime;
}
