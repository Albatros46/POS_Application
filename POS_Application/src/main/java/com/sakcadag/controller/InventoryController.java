package com.sakcadag.controller;
import com.sakcadag.entity.Inventory;
import com.sakcadag.service.interfaces.ICategoryService;
import com.sakcadag.service.interfaces.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {
    @Autowired
    private IInventoryService service;

    @PostMapping("/new-inventory")
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return service.saveInventory(inventory);
    }

    @GetMapping("/getAllInventories")
    public List<Inventory> getAllInventories() {
        return service.getAllInventories();
    }

    @GetMapping("/getByIdInventory/{id}")
    public Optional<Inventory> getInventoryById(@PathVariable Long id) {
        return service.getInventoryById(id);
    }

    @PutMapping("/updateInventory/{id}")
    public Inventory updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        return service.updateInventory(id, inventory);
    }

    @DeleteMapping("/delInventory/{id}")
    public void deleteInventory(@PathVariable Long id) {
        service.deleteInventory(id);
    }
}
