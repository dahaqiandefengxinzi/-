package com.example.shop.service;

import com.example.shop.entity.Cart;
import java.util.List;

public interface CartService {
    List<Cart> getCartList();
    
    void addToCart(Long productId, Integer quantity);
    
    void updateCart(Long id, Integer quantity, Integer selected);
    
    void deleteCartItem(Long id);
    
    void clearCart();
}
