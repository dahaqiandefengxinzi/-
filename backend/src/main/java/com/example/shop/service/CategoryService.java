package com.example.shop.service;

import com.example.shop.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getCategoryTree();
    
    List<Category> getAllCategories();
    
    Category getCategoryById(Long id);
    
    void addCategory(Category category);
    
    void updateCategory(Long id, Category category);
    
    void deleteCategory(Long id);
}
