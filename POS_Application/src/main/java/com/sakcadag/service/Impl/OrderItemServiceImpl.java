package com.sakcadag.service.Impl;

import com.sakcadag.entity.OrderItem;
import com.sakcadag.respository.OrderItemRepository;
import com.sakcadag.service.interfaces.IOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    public Optional<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public OrderItem updateOrderItem(Long id, OrderItem orderItem) {
        return orderItemRepository.findById(id)
                .map(existingOrderItem -> {
                    orderItem.setId(id);
                    return orderItemRepository.save(orderItem);
                })
                .orElseThrow(() -> new RuntimeException("OrderItem with id " + id + " not found"));
    }


    @Override
    public void deleteOrderItem(Long id) {
        orderItemRepository.deleteById(id);
    }
}