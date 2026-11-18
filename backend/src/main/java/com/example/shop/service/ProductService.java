package com.example.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.entity.Product;
import com.example.shop.dto.AddProductRequest;
import com.example.shop.dto.ProductDetailDTO;
import com.example.shop.dto.UpdateProductStatusRequest;

public interface ProductService {
    IPage<Product> getProductList(Long categoryId, String keyword, Integer page, Integer size, String sort);
    
    ProductDetailDTO getProductDetail(Long id);
    
    void addProduct(AddProductRequest request);
    
    void updateProduct(Long id, AddProductRequest request);
    
    void updateProductStatus(Long id, UpdateProductStatusRequest request);
    
    void deleteProduct(Long id);
    
    IPage<Product> getAdminProductList(String keyword, Long categoryId, Integer status, Integer page, Integer size);
}
