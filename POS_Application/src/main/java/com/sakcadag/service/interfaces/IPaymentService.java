package com.sakcadag.service.interfaces;

import com.sakcadag.dto.PaymentRequest;
import com.sakcadag.entity.Payment;

import java.util.List;

public interface IPaymentService {
    Payment pay(PaymentRequest request);
    List<Payment> getAllPayments();
    Payment getPaymentById(Long id);
}
