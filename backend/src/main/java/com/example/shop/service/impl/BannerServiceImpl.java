package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.entity.Banner;
import com.example.shop.mapper.BannerMapper;
import com.example.shop.service.BannerService;
import com.example.shop.dto.BannerRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {
    
    @Autowired
    private BannerMapper bannerMapper;
    
    @Override
    public List<Banner> getActiveBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1)
               .orderByAsc(Banner::getSort)
               .orderByDesc(Banner::getCreateTime);
        return bannerMapper.selectList(wrapper);
    }
    
    @Override
    public IPage<Banner> getBannerList(Integer page, Integer size) {
        Page<Banner> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Banner::getSort)
               .orderByDesc(Banner::getCreateTime);
        return bannerMapper.selectPage(pageParam, wrapper);
    }
    
    @Override
    public void addBanner(BannerRequest request) {
        Banner banner = new Banner();
        BeanUtils.copyProperties(request, banner);
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getIntervalTime() == null) {
            banner.setIntervalTime(5000);
        }
        bannerMapper.insert(banner);
    }
    
    @Override
    public void updateBanner(Long id, BannerRequest request) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new RuntimeException("Banner不存在");
        }
        BeanUtils.copyProperties(request, banner);
        bannerMapper.updateById(banner);
    }
    
    @Override
    public void deleteBanner(Long id) {
        bannerMapper.deleteById(id);
    }
    
    @Override
    public void updateBannerStatus(Long id, Integer status) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new RuntimeException("Banner不存在");
        }
        banner.setStatus(status);
        bannerMapper.updateById(banner);
    }
}
