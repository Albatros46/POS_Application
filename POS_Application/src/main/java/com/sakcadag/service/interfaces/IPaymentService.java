package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Payment;

import java.util.List;

public interface IPaymentService {
    Payment savePayment(Payment payment);
    List<Payment> getAllPayments();
    Payment getPaymentById(Long id);
    Payment updatePayment(Long id, Payment payment);
    void deletePayment(Long id);
}
