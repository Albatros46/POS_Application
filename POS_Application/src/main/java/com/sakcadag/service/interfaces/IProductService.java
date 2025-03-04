package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Product;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    Product saveProduct(Product product);
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}