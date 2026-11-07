package com.sakcadag.controller;
import com.sakcadag.entity.Category;
import com.sakcadag.entity.Product;
import com.sakcadag.exception.InvalidRequestException;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.CategoryRepository;
import com.sakcadag.service.interfaces.IProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private IProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;
    @PostMapping("/new-product")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        resolveCategory(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.saveProduct(product));
    }

    private void resolveCategory(Product product) {
        Category requested = product.getCategory();
        Long categoryId = requested == null ? null : requested.getId();
        if (categoryId == null) {
            throw new InvalidRequestException("Category id is required");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        product.setCategory(category);
    }

    @GetMapping("/getAllProduct")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/getByIdProduct/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/updateProduct/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        resolveCategory(product);
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/delProduct/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
