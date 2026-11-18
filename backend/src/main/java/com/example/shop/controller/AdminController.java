package com.example.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.common.Result;
import com.example.shop.dto.AddProductRequest;
import com.example.shop.dto.BannerRequest;
import com.example.shop.dto.CaptchaConfigDTO;
import com.example.shop.dto.ShipOrderRequest;
import com.example.shop.dto.UpdateProductStatusRequest;
import com.example.shop.entity.Banner;
import com.example.shop.entity.Category;
import com.example.shop.entity.Order;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.service.BannerService;
import com.example.shop.service.CaptchaService;
import com.example.shop.service.CategoryService;
import com.example.shop.service.OrderService;
import com.example.shop.service.ProductService;
import com.example.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理后台", description = "管理员专用接口，需要ADMIN角色权限")
@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private CaptchaService captchaService;

    @Operation(summary = "获取仪表盘统计数据", description = "获取用户数、商品数、订单数、销售额等统计数据")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        IPage<User> userPage = userService.getAdminUserList(null, null, null, 1, 1);
        stats.put("userCount", userPage.getTotal());
        
        IPage<Product> productPage = productService.getAdminProductList(null, null, null, 1, 1);
        stats.put("productCount", productPage.getTotal());
        
        IPage<Order> orderPage = orderService.getAdminOrderList(null, null, null, null, 1, 1);
        stats.put("orderCount", orderPage.getTotal());
        
        java.math.BigDecimal totalSales = orderService.sumCompletedSales();
        stats.put("totalSales", totalSales != null ? totalSales.toString() : "0.00");
        
        stats.put("pendingOrders", orderService.countByStatus(0));
        stats.put("paidOrders", orderService.countByStatus(1));
        stats.put("shippedOrders", orderService.countByStatus(2));
        stats.put("completedOrders", orderService.countByStatus(3));
        
        return Result.success(stats);
    }

    @Operation(summary = "获取分类列表", description = "获取所有分类的平铺列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/category/list")
    public Result<List<Category>> getCategoryList() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取分类树", description = "获取分类的树形结构")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/category/tree")
    public Result<List<Category>> getCategoryTree() {
        List<Category> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "添加分类", description = "添加新的商品分类")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @PostMapping("/category")
    public Result<Void> addCategory(
            @Parameter(description = "分类信息", required = true) @RequestBody Category category) {
        categoryService.addCategory(category);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新分类", description = "更新指定分类信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/category/{id}")
    public Result<Void> updateCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id,
            @Parameter(description = "分类信息", required = true) @RequestBody Category category) {
        categoryService.updateCategory(id, category);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除分类", description = "删除指定分类")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取商品列表", description = "分页获取商品列表（管理端）")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/product/list")
    public Result<IPage<Product>> getProductList(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态(0-下架,1-上架)") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        IPage<Product> productPage = productService.getAdminProductList(keyword, categoryId, status, page, size);
        return Result.success(productPage);
    }

    @Operation(summary = "添加商品", description = "添加新商品")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @PostMapping("/product")
    public Result<Void> addProduct(
            @Parameter(description = "商品信息", required = true) @Valid @RequestBody AddProductRequest request) {
        productService.addProduct(request);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新商品", description = "更新指定商品信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/product/{id}")
    public Result<Void> updateProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "商品信息", required = true) @Valid @RequestBody AddProductRequest request) {
        productService.updateProduct(id, request);
        return Result.success("更新成功");
    }

    @Operation(summary = "更新商品状态", description = "上架/下架商品")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/product/{id}/status")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态信息", required = true) @Valid @RequestBody UpdateProductStatusRequest request) {
        productService.updateProductStatus(id, request);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "删除商品", description = "删除指定商品")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @DeleteMapping("/product/{id}")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取订单列表", description = "分页获取订单列表（管理端）")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/order/list")
    public Result<IPage<Order>> getOrderList(
            @Parameter(description = "订单编号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        IPage<Order> orderPage = orderService.getAdminOrderList(orderNo, status, startDate, endDate, page, size);
        return Result.success(orderPage);
    }

    @Operation(summary = "获取订单详情", description = "根据订单编号获取订单详情")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/order/detail/{orderNo}")
    public Result<Order> getOrderDetail(
            @Parameter(description = "订单编号", required = true) @PathVariable String orderNo) {
        Order order = orderService.getAdminOrderDetail(orderNo);
        return Result.success(order);
    }

    @Operation(summary = "订单发货", description = "为订单填写物流信息并发货")
    @ApiResponse(responseCode = "200", description = "发货成功")
    @PutMapping("/order/{id}/ship")
    public Result<Void> shipOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @Parameter(description = "物流信息", required = true) @Valid @RequestBody ShipOrderRequest request) {
        orderService.shipOrder(id, request);
        return Result.success("发货成功");
    }

    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/user/list")
    public Result<IPage<User>> getUserList(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色(0-普通用户,1-管理员)") @RequestParam(required = false) Integer role,
            @Parameter(description = "状态(0-禁用,1-启用)") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        IPage<User> userPage = userService.getAdminUserList(keyword, role, status, page, size);
        return Result.success(userPage);
    }

    @Operation(summary = "更新用户信息", description = "管理员更新用户信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/user/{id}")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
        userService.adminUpdateUser(id, user);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取Banner列表", description = "分页获取Banner列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/banner/list")
    public Result<IPage<Banner>> getBannerList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        IPage<Banner> bannerPage = bannerService.getBannerList(page, size);
        return Result.success(bannerPage);
    }

    @Operation(summary = "添加Banner", description = "添加新的轮播图")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @PostMapping("/banner")
    public Result<Void> addBanner(
            @Parameter(description = "Banner信息", required = true) @RequestBody BannerRequest request) {
        bannerService.addBanner(request);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新Banner", description = "更新指定Banner信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/banner/{id}")
    public Result<Void> updateBanner(
            @Parameter(description = "Banner ID", required = true) @PathVariable Long id,
            @Parameter(description = "Banner信息", required = true) @RequestBody BannerRequest request) {
        bannerService.updateBanner(id, request);
        return Result.success("更新成功");
    }

    @Operation(summary = "更新Banner状态", description = "启用/禁用Banner")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/banner/{id}/status")
    public Result<Void> updateBannerStatus(
            @Parameter(description = "Banner ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态(0-禁用,1-启用)", required = true) @RequestParam Integer status) {
        bannerService.updateBannerStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "删除Banner", description = "删除指定Banner")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @DeleteMapping("/banner/{id}")
    public Result<Void> deleteBanner(
            @Parameter(description = "Banner ID", required = true) @PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取验证码配置", description = "获取系统验证码配置信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/captcha/config")
    public Result<Map<String, Object>> getCaptchaConfig() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", captchaService.isEnabled());
        data.put("config", captchaService.getConfig());
        return Result.success(data);
    }

    @Operation(summary = "更新验证码配置", description = "更新系统验证码配置")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/captcha/config")
    public Result<Void> updateCaptchaConfig(
            @Parameter(description = "配置信息", required = true) @Valid @RequestBody CaptchaConfigDTO dto) {
        captchaService.updateConfig(dto);
        return Result.success("配置更新成功");
    }
}
