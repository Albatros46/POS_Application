package com.sakcadag.controller;

import com.sakcadag.entity.OrderItem;
import com.sakcadag.service.interfaces.IOrderItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private IOrderItemService orderItemService;

    @PostMapping("/create")
    public ResponseEntity<OrderItem> createOrderItem(@Valid @RequestBody OrderItem orderItem) {
        OrderItem savedItem = orderItemService.saveOrderItem(orderItem);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    @GetMapping("/getByIdOrderItem/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id) {
        return ResponseEntity.ok(orderItemService.getOrderItemById(id));
    }

    @GetMapping("/getAllOrderItems")
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {
        List<OrderItem> items = orderItemService.getAllOrderItems();
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @PutMapping("/updateOrderItem/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(@PathVariable Long id, @Valid @RequestBody OrderItem orderItem) {
        OrderItem updatedItem = orderItemService.updateOrderItem(id, orderItem);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/delOrderItem/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
