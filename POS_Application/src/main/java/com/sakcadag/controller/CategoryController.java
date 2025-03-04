package com.sakcadag.controller;

import com.sakcadag.entity.Category;
import com.sakcadag.service.interfaces.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private ICategoryService service;

    @PostMapping("/new-category")
    public Category createCategory(@RequestBody Category category) {
        return service.saveCategory(category);
    }

    @GetMapping("/getAllCategories")
    public List<Category> getAllCategories() {
        return service.getAllCategories();
    }

    @GetMapping("/byIdCategory/{id}")
    public Optional<Category> getCategoryById(@PathVariable Long id) {
        return service.getCategoryById(id);
    }

    @PutMapping("/updateCategory/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return service.updateCategory(id, category);
    }

    @DeleteMapping("/delCategory/{id}")
    public void deleteCategory(@PathVariable Long id) {
        service.deleteCategory(id);
    }
}
