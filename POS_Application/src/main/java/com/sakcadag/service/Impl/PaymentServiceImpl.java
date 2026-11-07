package com.sakcadag.service.Impl;

import com.sakcadag.entity.Payment;

import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.PaymentRepository;
import com.sakcadag.service.interfaces.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements IPaymentService {
    @Autowired
    private PaymentRepository repository;

    @Override
    public Payment savePayment(Payment payment) {
        return repository.save(payment);
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

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        Payment existing = getPaymentById(id);
        existing.setOrder(payment.getOrder());
        existing.setAmount(payment.getAmount());
        return repository.save(existing);
    }

    @Override
    public void deletePayment(Long id) {
        repository.delete(getPaymentById(id));
    }
}
