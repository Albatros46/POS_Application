package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface IPaymentService {
    Payment savePayment(Payment payment);
    List<Payment> getAllPayments();
    Optional<Payment> getPaymentById(Long id);
    Payment updatePayment(Long id, Payment payment);
    void deletePayment(Long id);
}