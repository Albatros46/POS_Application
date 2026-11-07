package com.sakcadag.service.interfaces;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Order;

import java.util.List;

public interface ICustomerService {
    Customer saveCustomer(Customer customer);
    List<Customer> getAllCustomers();
    Customer getCustomerById(Long id);
    Customer updateCustomer(Long id, Customer customer);
    void deleteCustomer(Long id);
    List<Order> getCustomerOrders(Long customerId);
}
