package com.sakcadag.controller;
import com.sakcadag.entity.Category;
import com.sakcadag.entity.Product;
import com.sakcadag.respository.CategoryRepository;
import com.sakcadag.service.interfaces.ICategoryService;
import com.sakcadag.service.interfaces.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private IProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;
    @PostMapping("/new-product")
    public Product createProduct(@RequestBody Product product) {
       // return productService.saveProduct(product); //bu sekilde category kismi null geliyor.
        // Kategoriyi veritabanından al ve eksik bilgileri tamamla
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setCategory(category);
        return productService.saveProduct(product);
    }

    @GetMapping("/getAllProduct")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/getByIdProduct/{id}")
    public Optional<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/updateProduct/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/delProduct/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}