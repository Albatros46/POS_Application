package com.sakcadag.service.Impl;

import com.sakcadag.entity.Payment;

import com.sakcadag.respository.PaymentRepository;
import com.sakcadag.service.interfaces.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public Optional<Payment> getPaymentById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        return repository.findById(id)
                .map(existingPayment -> {
                    payment.setId(id);
                    return repository.save(payment);
                })
                .orElseThrow(() -> new RuntimeException("Payment with id " + id + " not found"));
    }

    @Override
    public void deletePayment(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new RuntimeException("Payment with id " + id + " not found");
        }
    }
}
