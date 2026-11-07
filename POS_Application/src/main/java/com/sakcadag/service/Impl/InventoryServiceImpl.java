package com.sakcadag.service.Impl;

import com.sakcadag.entity.Inventory;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.InventoryRepository;
import com.sakcadag.service.interfaces.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements IInventoryService {
    @Autowired
    private InventoryRepository repository;

    @Override
    public Inventory saveInventory(Inventory inventory) {
        return repository.save(inventory);
    }

    @Override
    public List<Inventory> getAllInventories() {
        return repository.findAll();
    }

    @Override
    public Inventory getInventoryById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    @Override
    public Inventory updateInventory(Long id, Inventory inventory) {
        Inventory existing = getInventoryById(id);
        existing.setProduct(inventory.getProduct());
        existing.setStockQuantity(inventory.getStockQuantity());
        return repository.save(existing);
    }

    @Override
    public void deleteInventory(Long id) {
        repository.delete(getInventoryById(id));
    }
}
