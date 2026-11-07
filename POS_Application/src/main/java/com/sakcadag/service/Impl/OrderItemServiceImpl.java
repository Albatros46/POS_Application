package com.sakcadag.service.Impl;

import com.sakcadag.entity.OrderItem;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.OrderItemRepository;
import com.sakcadag.service.interfaces.IOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OrderItemServiceImpl implements IOrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public OrderItem saveOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Override
    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", id));
    }

    @Override
    public OrderItem updateOrderItem(Long id, OrderItem orderItem) {
        OrderItem existing = getOrderItemById(id);
        existing.setProduct(orderItem.getProduct());
        existing.setQuantity(orderItem.getQuantity());
        return orderItemRepository.save(existing);
    }

    @Override
    public void deleteOrderItem(Long id) {
        orderItemRepository.delete(getOrderItemById(id));
    }
}
