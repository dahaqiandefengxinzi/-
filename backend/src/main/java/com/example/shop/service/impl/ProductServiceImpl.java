package com.example.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.dto.AddProductRequest;
import com.example.shop.dto.ProductDetailDTO;
import com.example.shop.dto.UpdateProductStatusRequest;
import com.example.shop.entity.Product;
import com.example.shop.entity.ProductDetail;
import com.example.shop.mapper.ProductDetailMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Override
    public IPage<Product> getProductList(Long categoryId, String keyword, Integer page, Integer size, String sort) {
        Page<Product> pageParam = new Page<>(page, size);
        return productMapper.selectProductPage(pageParam, categoryId, keyword, sort);
    }

    @Override
    public ProductDetailDTO getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        LambdaQueryWrapper<ProductDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDetail::getProductId, id);
        ProductDetail detail = productDetailMapper.selectOne(wrapper);
        
        ProductDetailDTO dto = new ProductDetailDTO();
        BeanUtil.copyProperties(product, dto);
        
        if (detail != null) {
            dto.setDescription(detail.getDescription());
            dto.setImages(JSONUtil.toList(detail.getImages(), String.class));
            dto.setSpecs(JSONUtil.parse(detail.getSpecs()));
        }
        
        return dto;
    }

    @Override
    @Transactional
    public void addProduct(AddProductRequest request) {
        Product product = new Product();
        BeanUtil.copyProperties(request, product);
        product.setStatus(1);
        product.setSales(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        
        ProductDetail detail = new ProductDetail();
        detail.setProductId(product.getId());
        detail.setDescription(request.getDescription());
        detail.setImages(JSONUtil.toJsonStr(request.getImages()));
        productDetailMapper.insert(detail);
    }

    @Override
    @Transactional
    public void updateProduct(Long id, AddProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getSubtitle() != null) {
            product.setSubtitle(request.getSubtitle());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getMainImage() != null) {
            product.setMainImage(request.getMainImage());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        
        if (request.getDescription() != null || request.getImages() != null) {
            LambdaQueryWrapper<ProductDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductDetail::getProductId, id);
            ProductDetail detail = productDetailMapper.selectOne(wrapper);
            
            if (detail != null) {
                if (request.getDescription() != null) {
                    detail.setDescription(request.getDescription());
                }
                if (request.getImages() != null) {
                    detail.setImages(JSONUtil.toJsonStr(request.getImages()));
                }
                productDetailMapper.updateById(detail);
            } else {
                detail = new ProductDetail();
                detail.setProductId(id);
                detail.setDescription(request.getDescription());
                detail.setImages(JSONUtil.toJsonStr(request.getImages()));
                productDetailMapper.insert(detail);
            }
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
        
        LambdaQueryWrapper<ProductDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDetail::getProductId, id);
        productDetailMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, UpdateProductStatusRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(request.getStatus());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public IPage<Product> getAdminProductList(String keyword, Long categoryId, Integer status, Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        return productMapper.selectAdminProductPage(pageParam, keyword, categoryId, status);
    }
}
