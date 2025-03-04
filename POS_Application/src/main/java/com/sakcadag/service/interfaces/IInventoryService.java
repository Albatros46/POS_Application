package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Inventory;

import java.util.List;
import java.util.Optional;

public interface IInventoryService {
    Inventory saveInventory(Inventory inventory);
    List<Inventory> getAllInventories();
    Optional<Inventory> getInventoryById(Long id);
    Inventory updateInventory(Long id, Inventory inventory);
    void deleteInventory(Long id);
}
