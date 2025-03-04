package com.sakcadag.service.Impl;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Order;
import com.sakcadag.respository.CustomerRepository;
import com.sakcadag.service.interfaces.ICustomerService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CustomerServiceImpl implements ICustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
            return customerRepository.findById(id).map(existingCustomer->{
                customer.setId(id);
                return customerRepository.save(customer);
            }).orElseThrow(()->new RuntimeException("Customer with id "+id+" not found!"));

    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
    public List<Order> getCustomerOrders(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return customer.getOrders();
    }

}
