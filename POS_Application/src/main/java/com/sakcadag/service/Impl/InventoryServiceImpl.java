package com.sakcadag.service.Impl;

import com.sakcadag.entity.Inventory;
import com.sakcadag.respository.InventoryRepository;
import com.sakcadag.service.interfaces.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public Optional<Inventory> getInventoryById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Inventory updateInventory(Long id, Inventory inventory) {
        return repository.findById(id)
                .map(existingInventory -> {
                    inventory.setId(id);
                    return repository.save(inventory);
                })
                .orElseThrow(() -> new RuntimeException("Inventory with id " + id + " not found"));
    }

    @Override
    public void deleteInventory(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new RuntimeException("Inventory with id " + id + " not found");
        }
    }
}