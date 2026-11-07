package com.sakcadag.controller;

import com.sakcadag.entity.OrderItem;
import com.sakcadag.service.interfaces.IOrderItemService;
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

    @GetMapping("/getByIdOrderItem/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id) {
        return ResponseEntity.ok(orderItemService.getOrderItemById(id));
    }

    @GetMapping("/getAllOrderItems")
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {
        List<OrderItem> items = orderItemService.getAllOrderItems();
        return new ResponseEntity<>(items, HttpStatus.OK);
    }
}
