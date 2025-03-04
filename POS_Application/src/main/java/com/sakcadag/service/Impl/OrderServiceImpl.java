package com.sakcadag.service.Impl;

import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderItem;
import com.sakcadag.respository.OrderItemRepository;
import com.sakcadag.respository.OrderRepository;
import com.sakcadag.service.interfaces.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
/*
    @Override
    public Order updateOrder(Long id, Order order) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    order.setId(id);
                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("Order with ID " + id + " not found"));
    }
    */
    @Override
    public Order updateOrder(Long id, Order orderDetails) {
        return orderRepository.findById(id).map(order -> {
            order.setCustomer(orderDetails.getCustomer());
            order.setOrderDate(orderDetails.getOrderDate());
            order.setOrderItems(orderDetails.getOrderItems());
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }
    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
    // **Müşteriye göre siparişleri listeleme**
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // **Belirli bir siparişin içindeki sipariş kalemlerini listeleme**
    public List<OrderItem> getOrderItemsByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}