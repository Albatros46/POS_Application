package com.sakcadag.service.interfaces;

import com.sakcadag.entity.OrderItem;

import java.util.List;

public interface IOrderItemService {
    List<OrderItem> getAllOrderItems();
    OrderItem getOrderItemById(Long id);
}
