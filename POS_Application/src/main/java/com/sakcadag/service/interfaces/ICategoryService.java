package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Category;

import java.util.List;

public interface ICategoryService {
    Category saveCategory(Category category);
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
}
