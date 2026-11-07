package com.sakcadag.controller;

import com.sakcadag.entity.Category;
import com.sakcadag.service.interfaces.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private ICategoryService service;

    @PostMapping("/new-category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveCategory(category));
    }

    @GetMapping("/getAllCategories")
    public List<Category> getAllCategories() {
        return service.getAllCategories();
    }

    @GetMapping("/byIdCategory/{id}")
    public Category getCategoryById(@PathVariable Long id) {
        return service.getCategoryById(id);
    }

    @PutMapping("/updateCategory/{id}")
    public Category updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        return service.updateCategory(id, category);
    }

    @DeleteMapping("/delCategory/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
