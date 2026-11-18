package com.example.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.common.Result;
import com.example.shop.dto.CreateOrderRequest;
import com.example.shop.dto.PayOrderRequest;
import com.example.shop.entity.Order;
import com.example.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "订单管理", description = "订单相关接口，需要用户登录")
@RestController
@RequestMapping("/api/order")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @Operation(summary = "创建订单", description = "从购物车创建新订单")
    @ApiResponse(responseCode = "200", description = "创建成功")
    @PostMapping
    public Result<Map<String, Object>> createOrder(
            @Parameter(description = "订单信息", required = true) @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderSn", order.getOrderSn());
        return Result.success(data);
    }

    @Operation(summary = "获取订单列表", description = "分页获取当前用户的订单列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping
    public Result<IPage<Order>> getOrderList(
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(orderService.getOrderList(status, page, size));
    }

    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详情")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @Operation(summary = "根据订单号获取订单", description = "根据订单编号获取订单详情")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/no/{orderNo}")
    public Result<Order> getOrderByOrderNo(
            @Parameter(description = "订单编号", required = true) @PathVariable String orderNo) {
        return Result.success(orderService.getOrderByOrderNo(orderNo));
    }

    @Operation(summary = "支付订单", description = "根据订单ID支付订单")
    @ApiResponse(responseCode = "200", description = "支付成功")
    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        orderService.payOrder(id, request.get("paymentType"));
        return Result.success("支付成功");
    }

    @Operation(summary = "根据订单号支付", description = "根据订单编号支付订单")
    @ApiResponse(responseCode = "200", description = "支付成功")
    @PostMapping("/no/{orderNo}/pay")
    public Result<Void> payOrderByOrderNo(
            @Parameter(description = "订单编号", required = true) @PathVariable String orderNo,
            @Parameter(description = "支付信息", required = true) @RequestBody PayOrderRequest request) {
        orderService.payOrderByOrderNo(orderNo, request.getPayMethod());
        return Result.success("支付成功");
    }

    @Operation(summary = "确认收货", description = "根据订单ID确认收货")
    @ApiResponse(responseCode = "200", description = "确认成功")
    @PostMapping("/{id}/receive")
    public Result<Void> receiveOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        orderService.receiveOrder(id);
        return Result.success("确认收货成功");
    }

    @Operation(summary = "根据订单号确认收货", description = "根据订单编号确认收货")
    @ApiResponse(responseCode = "200", description = "确认成功")
    @PostMapping("/no/{orderNo}/receive")
    public Result<Void> receiveOrderByOrderNo(
            @Parameter(description = "订单编号", required = true) @PathVariable String orderNo) {
        orderService.receiveOrderByOrderNo(orderNo);
        return Result.success("确认收货成功");
    }

    @Operation(summary = "取消订单", description = "根据订单ID取消订单")
    @ApiResponse(responseCode = "200", description = "取消成功")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("取消成功");
    }

    @Operation(summary = "根据订单号取消订单", description = "根据订单编号取消订单")
    @ApiResponse(responseCode = "200", description = "取消成功")
    @PostMapping("/no/{orderNo}/cancel")
    public Result<Void> cancelOrderByOrderNo(
            @Parameter(description = "订单编号", required = true) @PathVariable String orderNo) {
        orderService.cancelOrderByOrderNo(orderNo);
        return Result.success("取消成功");
    }
}
