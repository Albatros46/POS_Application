package com.sakcadag.service.interfaces;

import com.sakcadag.dto.CreateOrderRequest;
import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderItem;

import java.util.List;

public interface IOrderService {
    Order placeOrder(CreateOrderRequest request);
    Order cancelOrder(Long id);
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    List<Order> getOrdersByCustomer(Long customerId);
    List<OrderItem> getOrderItemsByOrder(Long orderId);
}
