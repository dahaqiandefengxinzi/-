package com.example.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    IPage<Product> selectProductPage(Page<Product> page, 
                                      @Param("categoryId") Long categoryId,
                                      @Param("keyword") String keyword,
                                      @Param("sort") String sort);
    
    IPage<Product> selectAdminProductPage(Page<Product> page,
                                           @Param("keyword") String keyword,
                                           @Param("categoryId") Long categoryId,
                                           @Param("status") Integer status);
}
