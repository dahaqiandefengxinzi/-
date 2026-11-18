package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.entity.Cart;
import com.example.shop.entity.Product;
import com.example.shop.mapper.CartMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.service.CartService;
import com.example.shop.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Cart> getCartList() {
        Long userId = SecurityUtil.getCurrentUserId();
        return cartMapper.selectCartWithProduct(userId);
    }

    @Override
    public void addToCart(Long productId, Integer quantity) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId);
        Cart existCart = cartMapper.selectOne(wrapper);
        
        if (existCart != null) {
            existCart.setQuantity(existCart.getQuantity() + quantity);
            existCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
    }

    @Override
    public void updateCart(Long id, Integer quantity, Integer selected) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null) {
            throw new RuntimeException("购物车项不存在");
        }
        
        if (!cart.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new RuntimeException("无权限操作");
        }
        
        if (quantity != null) {
            cart.setQuantity(quantity);
        }
        if (selected != null) {
            cart.setSelected(selected);
        }
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);
    }

    @Override
    public void deleteCartItem(Long id) {
        Cart cart = cartMapper.selectById(id);
        if (cart != null && cart.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            cartMapper.deleteById(id);
        }
    }

    @Override
    public void clearCart() {
        Long userId = SecurityUtil.getCurrentUserId();
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }
}
