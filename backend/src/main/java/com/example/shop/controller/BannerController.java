package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.entity.Banner;
import com.example.shop.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Banner轮播图", description = "首页Banner轮播图公开接口")
@RestController
@RequestMapping("/api/banner")
public class BannerController {
    
    @Autowired
    private BannerService bannerService;

    @Operation(summary = "获取启用的Banner列表", description = "获取所有启用状态的Banner轮播图，按排序字段排序")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/active")
    public Result<List<Banner>> getActiveBanners() {
        return Result.success(bannerService.getActiveBanners());
    }
}
