package com.sakcadag.service.Impl;

import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderItem;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.OrderItemRepository;
import com.sakcadag.respository.OrderRepository;
import com.sakcadag.service.interfaces.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    // Order items are managed through the order-items endpoints, so only the order's own fields are copied here.
    @Override
    public Order updateOrder(Long id, Order orderDetails) {
        Order existing = getOrderById(id);
        existing.setCustomer(orderDetails.getCustomer());
        existing.setOrderDate(orderDetails.getOrderDate());
        return orderRepository.save(existing);
    }
    @Override
    public void deleteOrder(Long id) {
        orderRepository.delete(getOrderById(id));
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
