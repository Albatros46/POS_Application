package com.sakcadag.controller;
import com.sakcadag.entity.Customer;
import com.sakcadag.entity.Order;

import com.sakcadag.service.interfaces.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired
    private ICustomerService customerService;

    @PostMapping("/new-customer")
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.saveCustomer(customer);
    }

    @GetMapping("/getAllCustomer")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/getByIdCustomer/{id}")
    public Optional<Customer> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PutMapping("/updateCustomer/{id}")
    public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return customerService.updateCustomer(id, customer);
    }

    @DeleteMapping("/delCustomer/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }
    @GetMapping("/orders/{id}")
    public List<Order> getCustomerOrders(@PathVariable Long id) {
        return customerService.getCustomerOrders(id);
    }
}
