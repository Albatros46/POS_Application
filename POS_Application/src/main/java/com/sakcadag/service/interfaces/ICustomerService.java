package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Order;

import java.util.List;
import java.util.Optional;

public interface ICustomerService {
    Customer saveCustomer(Customer customer);
    List<Customer> getAllCustomers();
    Optional<Customer> getCustomerById(Long id);
    Customer updateCustomer(Long id, Customer customer);
    void deleteCustomer(Long id);
    List<Order> getCustomerOrders(Long customerId);
}