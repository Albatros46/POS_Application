package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Order;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    Order saveOrder(Order order);
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Order updateOrder(Long id, Order order);
    void deleteOrder(Long id);
}