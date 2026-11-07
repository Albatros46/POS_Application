package com.sakcadag.controller;
import com.sakcadag.entity.Inventory;
import com.sakcadag.entity.Product;
import com.sakcadag.exception.InvalidRequestException;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.ProductRepository;
import com.sakcadag.service.interfaces.IInventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {
    @Autowired
    private IInventoryService service;
    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/new-inventory")
    public ResponseEntity<Inventory> createInventory(@Valid @RequestBody Inventory inventory) {
        resolveProduct(inventory);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveInventory(inventory));
    }

    private void resolveProduct(Inventory inventory) {
        Product requested = inventory.getProduct();
        Long productId = requested == null ? null : requested.getId();
        if (productId == null) {
            throw new InvalidRequestException("Product id is required");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        inventory.setProduct(product);
    }

    @GetMapping("/getAllInventories")
    public List<Inventory> getAllInventories() {
        return service.getAllInventories();
    }

    @GetMapping("/getByIdInventory/{id}")
    public Inventory getInventoryById(@PathVariable Long id) {
        return service.getInventoryById(id);
    }

    @PutMapping("/updateInventory/{id}")
    public Inventory updateInventory(@PathVariable Long id, @Valid @RequestBody Inventory inventory) {
        resolveProduct(inventory);
        return service.updateInventory(id, inventory);
    }

    @DeleteMapping("/delInventory/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        service.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}
