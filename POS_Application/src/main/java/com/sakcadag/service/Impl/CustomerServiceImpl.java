package com.sakcadag.service.Impl;

import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Order;
import com.sakcadag.exception.ResourceNotFoundException;
import com.sakcadag.respository.CustomerRepository;
import com.sakcadag.service.interfaces.ICustomerService;

import java.util.List;

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
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = getCustomerById(id);
        existing.setFirstName(customer.getFirstName());
        existing.setLastName(customer.getLastName());
        existing.setEmail(customer.getEmail());
        existing.setPhone(customer.getPhone());
        return customerRepository.save(existing);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.delete(getCustomerById(id));
    }

    @Override
    public List<Order> getCustomerOrders(Long customerId) {
        return getCustomerById(customerId).getOrders();
    }

}
