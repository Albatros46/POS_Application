package com.sakcadag.controller;
import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderItem;
import com.sakcadag.service.Impl.OrderServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService;

    @PostMapping("/new-order")
    public Order createOrder(@RequestBody Order order) {
        return orderService.saveOrder(order);
    }

    @GetMapping("getAllOrders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/getByIdOrder/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(order -> ResponseEntity.ok().body(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/updateOrder/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        try {
            Order updatedOrder = orderService.updateOrder(id, order);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delOrder/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // **Müşteriye göre siparişleri listeleme**
    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    // **Belirli bir siparişin sipariş kalemlerini listeleme**
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItemsByOrder(@PathVariable Long orderId) {
        return orderService.getOrderItemsByOrder(orderId);
    }
}
