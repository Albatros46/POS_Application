package com.sakcadag.service.Impl;

import com.sakcadag.dto.PaymentRequest;
import com.sakcadag.entity.Order;
import com.sakcadag.entity.OrderStatus;
import com.sakcadag.entity.Payment;
import com.sakcadag.exception.BusinessRuleException;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.OrderRepository;
import com.sakcadag.respository.PaymentRepository;
import com.sakcadag.service.interfaces.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentServiceImpl implements IPaymentService {
    @Autowired
    private PaymentRepository repository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public Payment pay(PaymentRequest request) {
        Long orderId = request.orderId();
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessRuleException("Order " + orderId + " is not open for payment");
        }
        if (request.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new BusinessRuleException("Payment must equal the order total of " + order.getTotalAmount());
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.amount());
        Payment saved = repository.save(payment);

        order.setStatus(OrderStatus.PAID);
        return saved;
    }

    @Override
    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    @Override
    public Payment getPaymentById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }
}
