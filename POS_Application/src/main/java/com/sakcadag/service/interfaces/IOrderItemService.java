package com.sakcadag.service.interfaces;

import com.sakcadag.entity.OrderItem;

import java.util.List;

public interface IOrderItemService {
    OrderItem saveOrderItem(OrderItem orderItem);
    List<OrderItem> getAllOrderItems();
    OrderItem getOrderItemById(Long id);
    OrderItem updateOrderItem(Long id, OrderItem orderItem);
    void deleteOrderItem(Long id);
}
