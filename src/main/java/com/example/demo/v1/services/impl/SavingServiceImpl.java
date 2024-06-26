package com.example.demo.v1.services.impl;

import com.example.demo.v1.configs.EmailService;
import com.example.demo.v1.dtos.structured.MessageDTO;
import com.example.demo.v1.dtos.structured.SavingDTO;
import com.example.demo.v1.enumerations.ETransactionType;
import com.example.demo.v1.models.Customer;
import com.example.demo.v1.models.Message;
import com.example.demo.v1.models.Saving;
import com.example.demo.v1.repositories.ICustomerRepository;
import com.example.demo.v1.repositories.ISavingRepository;
import com.example.demo.v1.services.IMessageService;
import com.example.demo.v1.services.ISavingService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SavingServiceImpl implements ISavingService {
    @Autowired
    private ISavingRepository savingRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public Saving save(SavingDTO savingDTO) {
        if (savingDTO.getType() != null && !savingDTO.getType().equals(ETransactionType.SAVING)) {
            throw new IllegalArgumentException("Type must be set to Saving");
        }
        if (savingDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount should be greater than zero");
        }
        Saving saving = modelMapper.map(savingDTO, Saving.class);
        Optional<Customer> customerOpt = customerRepository.findById(savingDTO.getCustomer());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (customer.getAccount().equals(savingDTO.getAccount())) {
                customer.setBalance(customer.getBalance() + savingDTO.getAmount());
                customerRepository.save(customer);
                saving.setCustomer(customer);
                Saving saved = savingRepository.save(saving);
                String imageURL = "https://i.postimg.cc/WzW9K5Mq/Screenshot-2024-06-25-at-23-00-42.png";
                String messageContent = "Dear " + customer.getFirstName() + " " + customer.getLastName() +  " ,\n\nYour savings of " + saved.getAmount() + " on your account " + saved.getAccount() + " has been completed successfully";
                String emailContent = "<html>" +
                        "<body>" +
                        "<p>Dear " + customer.getFirstName() + " " + customer.getLastName() + ",</p>" +
                        "<p>Your savings of " + saved.getAmount() + " RWF on your account " + saved.getAccount() +
                        " has been completed successfully. Your new balance is " + customer.getBalance() + " RWF </p>" +
                        "<img src='" + imageURL + "' alt='Image' />" +
                        "<br><br><br><br><br>" +
                        "<p>Thank you for banking with us. </p>" +
                        "<p>Regards, </p>" +
                        "<p>Banking Management System - Cedrick</p>" +
                        "</body>" +
                        "</html>";
                try {
                    emailService.sendHtmlEmail(customer.getEmail(), "Saving Successful", emailContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.setMessage(messageContent);
                message.setCustomer(customer);
                messageService.save(message);
                return saved;
            } else {
                throw new IllegalArgumentException("Account number does not match customer account");
            }
        } else {
            throw new IllegalArgumentException("Customer not found");
        }
    }

    @Override
    @Transactional
    public Saving update(UUID id, SavingDTO savingDTO) {
        if (id == null) {
            throw new IllegalArgumentException("The given uuid id must not be null");
        }
        if (savingDTO.getType() != null && !savingDTO.getType().equals(ETransactionType.SAVING)) {
            throw new IllegalArgumentException("Type must be set to Saving");
        }
        if (savingDTO.getAmount() != null && savingDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount should be greater than zero");
        }

        Optional<Saving> savingOpt = savingRepository.findById(id);
        if (savingOpt.isPresent()) {
            Saving existingSaving = savingOpt.get();

            // Fetch the customer if customer ID is provided in the DTO
            if (savingDTO.getCustomer() != null) {
                Optional<Customer> customerOpt = customerRepository.findById(savingDTO.getCustomer());
                if (customerOpt.isPresent()) {
                    Customer customer = customerOpt.get();
                    double originalAmount = existingSaving.getAmount();
                    double newAmount = savingDTO.getAmount() != null ? savingDTO.getAmount() : originalAmount;

                    // Update customer balance if amount is provided
                    if (savingDTO.getAmount() != null) {
                        customer.setBalance(customer.getBalance() - originalAmount + newAmount);
                        customerRepository.save(customer);
                    }

                    // Update saving details if provided
                    if (savingDTO.getAccount() != null) {
                        existingSaving.setAccount(savingDTO.getAccount());
                    }
                    if (savingDTO.getAmount() != null) {
                        existingSaving.setAmount(newAmount);
                    }
                    if (savingDTO.getType() != null) {
                        existingSaving.setType(savingDTO.getType());
                    }

                    Saving saved = savingRepository.save(existingSaving);

                    String emailContent = "Dear " + customer.getFirstName() + " " + customer.getLastName() +  " ,\n\nYour savings of " + saved.getAmount() + " on your account " + saved.getAccount() + " has been completed successfully";
                    emailService.sendSimpleEmail(customer.getEmail(), "Saving Successful", emailContent);
                    return saved;
                } else {
                    throw new IllegalArgumentException("Customer not found");
                }
            } else {
                // Update saving details without customer check
                if (savingDTO.getAccount() != null) {
                    existingSaving.setAccount(savingDTO.getAccount());
                }
                if (savingDTO.getAmount() != null) {
                    double originalAmount = existingSaving.getAmount();
                    double newAmount = savingDTO.getAmount();
                    Customer customer = existingSaving.getCustomer();
                    customer.setBalance(customer.getBalance() - originalAmount + newAmount);
                    customerRepository.save(customer);
                    existingSaving.setAmount(newAmount);
                }
                if (savingDTO.getType() != null) {
                    existingSaving.setType(savingDTO.getType());
                }

                Saving saved = savingRepository.save(existingSaving);

                Customer customer = existingSaving.getCustomer();
                String emailContent = "Dear " + customer.getFirstName() + " " + customer.getLastName() +  " ,\n\nYour savings of " + saved.getAmount() + " on your account " + saved.getAccount() + " has been completed successfully";
                emailService.sendSimpleEmail(customer.getEmail(), "Saving Successful", emailContent);

                return saved;
            }
        } else {
            throw new IllegalArgumentException("Saving not found");
        }
    }



    @Override
    public Optional<Saving> getById(UUID id) {
        return savingRepository.findById(id);
    }

    @Override
    public boolean delete(UUID id) {
        Optional<Saving> savingOpt = savingRepository.findById(id);
        if (savingOpt.isPresent()) {
            Saving saving = savingOpt.get();

            // Fetch the customer
            Optional<Customer> customerOpt = customerRepository.findById(saving.getCustomer().getId());
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                customer.setBalance(customer.getBalance() - saving.getAmount());
                customerRepository.save(customer);
            }

            savingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Saving> getAll() {
        return savingRepository.findAll();
    }
}
