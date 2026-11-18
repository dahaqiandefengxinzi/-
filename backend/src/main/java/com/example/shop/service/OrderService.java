package com.example.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.entity.Order;
import com.example.shop.dto.CreateOrderRequest;
import com.example.shop.dto.ShipOrderRequest;
import java.util.List;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);
    
    IPage<Order> getOrderList(Integer status, Integer page, Integer size);
    
    Order getOrderDetail(Long id);
    
    Order getOrderByOrderNo(String orderNo);
    
    void payOrder(Long id, Integer paymentType);
    
    void payOrderByOrderNo(String orderNo, String payMethod);
    
    void receiveOrder(Long id);
    
    void receiveOrderByOrderNo(String orderNo);
    
    void cancelOrder(Long id);
    
    void cancelOrderByOrderNo(String orderNo);
    
    void shipOrder(Long id, ShipOrderRequest request);
    
    IPage<Order> getAdminOrderList(String orderNo, Integer status, String startDate, String endDate, Integer page, Integer size);
    
    Order getAdminOrderDetail(String orderNo);
    
    int countByStatus(Integer status);
    
    java.math.BigDecimal sumCompletedSales();
}
