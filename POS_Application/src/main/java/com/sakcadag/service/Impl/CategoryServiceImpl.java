package com.sakcadag.service.Impl;

import com.sakcadag.entity.Category;
import com.sakcadag.respository.CategoryRepository;
import com.sakcadag.service.interfaces.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category updateCategory(Long id, Category category) {
            return categoryRepository.findById(id)
                .map(existingCategory->{
                    category.setId(id);
                    return categoryRepository.save(category);
                }).orElseThrow(()->new RuntimeException("Category with id "+id+" not found!"));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
