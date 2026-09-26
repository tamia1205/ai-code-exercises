package com.example.crm;

import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    private final List<Customer> customers = new ArrayList<>();

    public List<Customer> findAll() {
        return customers;
    }

    public void saveAll(List<Customer> updatedCustomers) {
        updatedCustomers.forEach(customer -> {
            int index = customers.indexOf(customer);
            if (index != -1) {
                customers.set(index, customer);
            } else {
                customers.add(customer);
            }
        });
    }
}
