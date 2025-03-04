package com.sakcadag.service.interfaces;

import com.sakcadag.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface IOrderItemService {
    OrderItem saveOrderItem(OrderItem orderItem);
    List<OrderItem> getAllOrderItems();
    Optional<OrderItem> getOrderItemById(Long id);
    OrderItem updateOrderItem(Long id, OrderItem orderItem);
    void deleteOrderItem(Long id);
}