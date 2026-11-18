package com.example.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.common.Result;
import com.example.shop.dto.ProductDetailDTO;
import com.example.shop.entity.Category;
import com.example.shop.entity.Product;
import com.example.shop.service.CategoryService;
import com.example.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "商品管理", description = "商品列表、分类、详情等公开接口")
@RestController
@RequestMapping("/api/product")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "获取商品分类树", description = "获取所有商品分类的树形结构")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/category")
    public Result<List<Category>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "获取商品列表", description = "分页获取商品列表，支持按分类、关键词筛选和排序")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/list")
    public Result<IPage<Product>> getProductList(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序方式(price-asc/price-desc/sales)") @RequestParam(required = false) String sort) {
        IPage<Product> result = productService.getProductList(categoryId, keyword, page, size, sort);
        return Result.success(result);
    }

    @Operation(summary = "获取商品详情", description = "根据商品ID获取商品详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "商品不存在")
    @GetMapping("/detail/{id}")
    public Result<ProductDetailDTO> getProductDetail(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}
