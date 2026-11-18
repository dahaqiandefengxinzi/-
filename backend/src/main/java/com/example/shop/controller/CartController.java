package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.dto.AddCartRequest;
import com.example.shop.dto.UpdateCartRequest;
import com.example.shop.entity.Cart;
import com.example.shop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@Tag(name = "购物车管理", description = "购物车相关接口，需要用户登录")
@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {
    
    @Autowired
    private CartService cartService;

    @Operation(summary = "获取购物车列表", description = "获取当前用户的购物车商品列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping
    public Result<List<Cart>> getCartList() {
        return Result.success(cartService.getCartList());
    }

    @Operation(summary = "添加商品到购物车", description = "将指定商品添加到购物车")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @PostMapping
    public Result<Void> addToCart(
            @Parameter(description = "购物车信息", required = true) @Valid @RequestBody AddCartRequest request) {
        cartService.addToCart(request.getProductId(), request.getQuantity());
        return Result.success("添加成功");
    }

    @Operation(summary = "更新购物车商品", description = "更新购物车中商品的数量或选中状态")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/{id}")
    public Result<Void> updateCart(
            @Parameter(description = "购物车项ID", required = true) @PathVariable Long id,
            @Parameter(description = "更新信息", required = true) @RequestBody UpdateCartRequest request) {
        cartService.updateCart(id, request.getQuantity(), request.getSelected());
        return Result.success("更新成功");
    }

    @Operation(summary = "删除购物车商品", description = "从购物车中删除指定商品")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCartItem(
            @Parameter(description = "购物车项ID", required = true) @PathVariable Long id) {
        cartService.deleteCartItem(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "清空购物车", description = "清空当前用户购物车中的所有商品")
    @ApiResponse(responseCode = "200", description = "清空成功")
    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        cartService.clearCart();
        return Result.success("清空成功");
    }
}
