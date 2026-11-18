package com.example.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.dto.CreateOrderRequest;
import com.example.shop.dto.ShipOrderRequest;
import com.example.shop.entity.*;
import com.example.shop.mapper.*;
import com.example.shop.service.OrderService;
import com.example.shop.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private AddressMapper addressMapper;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null) {
            throw new RuntimeException("地址不存在");
        }
        
        List<Cart> carts = cartMapper.selectCartWithProduct(userId);
        if (carts.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : carts) {
            if (request.getCartIds().contains(cart.getId())) {
                totalAmount = totalAmount.add(cart.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            }
        }
        
        Order order = new Order();
        order.setOrderSn(generateOrderSn());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setPaymentType(request.getPaymentType());
        order.setShippingAddress(JSONUtil.toJsonStr(address));
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        
        for (Cart cart : carts) {
            if (request.getCartIds().contains(cart.getId())) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setProductId(cart.getProductId());
                item.setProductName(cart.getProductName());
                item.setProductPrice(cart.getPrice());
                item.setQuantity(cart.getQuantity());
                item.setProductImage(cart.getProductImage());
                orderItemMapper.insert(item);
                
                Product product = productMapper.selectById(cart.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() - cart.getQuantity());
                    product.setSales(product.getSales() + cart.getQuantity());
                    productMapper.updateById(product);
                }
                
                cartMapper.deleteById(cart.getId());
            }
        }
        
        return order;
    }

    @Override
    public IPage<Order> getOrderList(Integer status, Integer page, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<Order> pageParam = new Page<>(page, size);
        return orderMapper.selectOrderPage(pageParam, userId, status);
    }

    @Override
    public Order getOrderDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new RuntimeException("无权限查看");
        }
        
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        order.setOrderItems(items);
        
        return order;
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderSn, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new RuntimeException("无权限查看");
        }
        
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        order.setOrderItems(items);
        
        return order;
    }

    @Override
    @Transactional
    public void payOrder(Long id, Integer paymentType) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(1);
        order.setPaymentType(paymentType);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void payOrderByOrderNo(String orderNo, String payMethod) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderSn, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不正确");
        }
        
        int paymentType = "alipay".equals(payMethod) ? 1 : 2;
        
        order.setStatus(1);
        order.setPaymentType(paymentType);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void receiveOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void receiveOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderSn, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new RuntimeException("无权限操作");
        }
        
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("只能取消待付款订单");
        }
        
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public void cancelOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderSn, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new RuntimeException("无权限操作");
        }
        
        if (order.getStatus() != 0) {
            throw new RuntimeException("只能取消待付款订单");
        }
        
        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public void shipOrder(Long id, ShipOrderRequest request) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确");
        }
        
        order.setStatus(2);
        order.setShippingCompany(request.getShippingCompany());
        order.setShippingNumber(request.getShippingNumber());
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
    
    @Override
    public IPage<Order> getAdminOrderList(String orderNo, Integer status, String startDate, String endDate, Integer page, Integer size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(Order::getOrderSn, orderNo);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        IPage<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
        
        for (Order order : orderPage.getRecords()) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            order.setOrderItems(items);
        }
        
        return orderPage;
    }
    
    @Override
    public Order getAdminOrderDetail(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderSn, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        order.setOrderItems(items);
        
        return order;
    }

    @Override
    public int countByStatus(Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        return Math.toIntExact(orderMapper.selectCount(wrapper));
    }

    @Override
    public java.math.BigDecimal sumCompletedSales() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 3);
        List<Order> completedOrders = orderMapper.selectList(wrapper);
        return completedOrders.stream()
                .map(Order::getPayAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private String generateOrderSn() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = IdUtil.randomUUID().substring(0, 6);
        return timestamp + random;
    }
}
