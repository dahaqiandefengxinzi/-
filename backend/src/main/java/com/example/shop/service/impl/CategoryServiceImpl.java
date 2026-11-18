package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.entity.Category;
import com.example.shop.mapper.CategoryMapper;
import com.example.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1)
        );
        
        Map<Long, List<Category>> groupedByParent = allCategories.stream()
            .collect(Collectors.groupingBy(Category::getParentId));
        
        List<Category> rootCategories = groupedByParent.getOrDefault(0L, new ArrayList<>());
        
        buildTree(rootCategories, groupedByParent);
        
        return rootCategories;
    }
    
    private void buildTree(List<Category> categories, Map<Long, List<Category>> groupedByParent) {
        if (categories == null) return;
        
        for (Category category : categories) {
            List<Category> children = groupedByParent.get(category.getId());
            if (children != null && !children.isEmpty()) {
                category.setChildren(children);
                buildTree(children, groupedByParent);
            }
        }
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort)
        );
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void addCategory(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getLevel() == null) {
            if (category.getParentId() == 0L) {
                category.setLevel(1);
            } else {
                Category parent = categoryMapper.selectById(category.getParentId());
                if (parent != null) {
                    category.setLevel(parent.getLevel() + 1);
                }
            }
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, Category category) {
        Category existCategory = categoryMapper.selectById(id);
        if (existCategory == null) {
            throw new RuntimeException("分类不存在");
        }
        
        if (category.getName() != null) {
            existCategory.setName(category.getName());
        }
        if (category.getParentId() != null) {
            existCategory.setParentId(category.getParentId());
        }
        if (category.getLevel() != null) {
            existCategory.setLevel(category.getLevel());
        }
        if (category.getSort() != null) {
            existCategory.setSort(category.getSort());
        }
        if (category.getIcon() != null) {
            existCategory.setIcon(category.getIcon());
        }
        if (category.getStatus() != null) {
            existCategory.setStatus(category.getStatus());
        }
        
        existCategory.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(existCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
