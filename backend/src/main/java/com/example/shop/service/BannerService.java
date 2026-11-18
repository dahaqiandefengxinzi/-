package com.example.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.entity.Banner;
import com.example.shop.dto.BannerRequest;
import java.util.List;

public interface BannerService {
    List<Banner> getActiveBanners();
    
    IPage<Banner> getBannerList(Integer page, Integer size);
    
    void addBanner(BannerRequest request);
    
    void updateBanner(Long id, BannerRequest request);
    
    void deleteBanner(Long id);
    
    void updateBannerStatus(Long id, Integer status);
}
