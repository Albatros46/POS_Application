package com.sakcadag.service.Impl;

import com.sakcadag.entity.Product;
import com.sakcadag.respository.ProductRepository;
import com.sakcadag.service.interfaces.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
           return productRepository.findById(id).map(existingProduct->{
               product.setId(id);
               return productRepository.save(product);
           }).orElseThrow(()->new RuntimeException("Product with id "+id+" not found!"));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}