package com.example.demo.v1.services.impl;

import com.example.demo.v1.dtos.structured.CustomerDTO;
import com.example.demo.v1.models.Customer;
import com.example.demo.v1.repositories.ICustomerRepository;
import com.example.demo.v1.services.ICustomerService;
import com.example.demo.v1.utils.Utilities;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements ICustomerService {
    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public Customer save(CustomerDTO customerDTO) {
        if (!Utilities.isValidDateFormat(customerDTO.getDob())) {
            throw new IllegalArgumentException("Date of birth must be in yyyy-mm-dd format & in the past.");
        }
        // Check the telephone number to be at max 15 and least 10 characters with only numbers allowed
        if (!customerDTO.getMobile().matches("^[0-9]{10,12}$")) {
            throw new IllegalArgumentException("Mobile number must be at least 10 and at most 12 characters long and contain only numbers");
        }
        Customer customer = modelMapper.map(customerDTO, Customer.class);
        customer.setAccount(Utilities.generateAccountNumber(customer.getEmail()));
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer update(UUID id, CustomerDTO customerDTO) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isPresent()) {
            Customer existingCustomer = customerOptional.get();

            // Update only if the field is not null or empty
            if (customerDTO.getFirstName() != null && !customerDTO.getFirstName().isEmpty()) {
                existingCustomer.setFirstName(customerDTO.getFirstName());
            }
            if (customerDTO.getLastName() != null && !customerDTO.getLastName().isEmpty()) {
                existingCustomer.setLastName(customerDTO.getLastName());
            }
            if (customerDTO.getDob() != null && !customerDTO.getDob().isEmpty()) {
                if (!Utilities.isValidDateFormat(customerDTO.getDob())) {
                    throw new IllegalArgumentException("Date of birth must be in yyyy-mm-dd format & in the past.");
                }
                existingCustomer.setDob(customerDTO.getDob());
            }
            if (customerDTO.getMobile() != null && !customerDTO.getMobile().isEmpty()) {
                if (!customerDTO.getMobile().matches("^[0-9]{10,12}$")) {
                    throw new IllegalArgumentException("Mobile number must be at least 10 and at most 12 characters long and contain only numbers");
                }
                existingCustomer.setMobile(customerDTO.getMobile());
            }
            if (customerDTO.getEmail() != null && !customerDTO.getEmail().isEmpty()) {
                existingCustomer.setEmail(customerDTO.getEmail());
            }
            if (customerDTO.getBalance() != null) {
                existingCustomer.setBalance(customerDTO.getBalance());
            }

            return customerRepository.save(existingCustomer);
        }
        return null;
    }

    @Override
    public Optional<Customer> getById(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    public boolean delete(UUID id) {
        try {
            customerRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }
}
