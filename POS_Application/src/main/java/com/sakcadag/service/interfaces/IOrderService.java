package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Order;

import java.util.List;

public interface IOrderService {
    Order saveOrder(Order order);
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    Order updateOrder(Long id, Order order);
    void deleteOrder(Long id);
}
