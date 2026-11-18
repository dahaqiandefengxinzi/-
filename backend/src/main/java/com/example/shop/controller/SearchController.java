package com.example.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.common.Result;
import com.example.shop.entity.Product;
import com.example.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品搜索", description = "商品搜索公开接口")
@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @Autowired
    private ProductService productService;

    @Operation(summary = "搜索商品", description = "根据关键词搜索商品，支持分页和排序")
    @ApiResponse(responseCode = "200", description = "搜索成功")
    @GetMapping
    public Result<IPage<Product>> search(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序方式(price-asc/price-desc/sales)") @RequestParam(required = false) String sort) {
        IPage<Product> result = productService.getProductList(null, keyword, page, size, sort);
        return Result.success(result);
    }
}
