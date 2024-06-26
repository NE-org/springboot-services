package com.example.demo.v1.services.impl;
import com.example.demo.v1.configs.EmailService;
import com.example.demo.v1.dtos.structured.BankingDTO;
import com.example.demo.v1.enumerations.ETransactionType;
import com.example.demo.v1.models.Banking;
import com.example.demo.v1.models.Customer;
import com.example.demo.v1.models.Message;
import com.example.demo.v1.repositories.IBankingRepository;
import com.example.demo.v1.repositories.ICustomerRepository;
import com.example.demo.v1.services.IBankingService;
import com.example.demo.v1.services.IMessageService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankingServiceImpl implements IBankingService {
    @Autowired
    private IBankingRepository bankingRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmailService emailService;

    @Autowired
    private IMessageService messageService;

    @Override
    @Transactional
    public Banking save(BankingDTO bankingDTO) {
        if (bankingDTO.getType() != null && !bankingDTO.getType().equals(ETransactionType.TRANSFER)) {
            throw new IllegalArgumentException("Type must be set to Transfer");
        }
        if (bankingDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount should be greater than zero");
        }
        Optional<Customer> customerOpt = customerRepository.findById(bankingDTO.getCustomer());
        Optional<Customer> receiptCustomerOpt = customerRepository.findById(bankingDTO.getReceiptCustomer());
        if (customerOpt.isPresent() && receiptCustomerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            Customer receiptCustomer = receiptCustomerOpt.get();

            if (!customer.getAccount().equals(bankingDTO.getAccount())) {
                throw new IllegalArgumentException("Account number does not match customer account");
            }
            if (!receiptCustomer.getAccount().equals(bankingDTO.getReceiptAccount())) {
                throw new IllegalArgumentException("Receipt account number does not match receipt customer account");
            }

            if (customer.getBalance() >= bankingDTO.getAmount()) {
                // Deduct amount from customer's balance
                customer.setBalance(customer.getBalance() - bankingDTO.getAmount());
                customerRepository.save(customer);

                // Add amount to receipt customer's balance
                receiptCustomer.setBalance(receiptCustomer.getBalance() + bankingDTO.getAmount());
                customerRepository.save(receiptCustomer);

                // Map DTO to entity and set relationships
                Banking banking = modelMapper.map(bankingDTO, Banking.class);
                banking.setCustomer(customer);
                banking.setReceiptCustomer(receiptCustomer);

                // Save the banking transaction
                Banking savedBanking = bankingRepository.save(banking);

                String imageURL = "https://i.postimg.cc/WzW9K5Mq/Screenshot-2024-06-25-at-23-00-42.png";
                String messageEmailContent = "Dear " + customer.getFirstName() + " " + customer.getLastName() +
                        ",\n\nYour transfer of " + banking.getAmount() + " to account " + banking.getReceiptAccount() +
                        " has been completed successfully. Your new balance " + customer.getBalance();

                String emailContent =
                        "<html>" +
                                "<body>" +
                                "<p>Dear " + customer.getFirstName() + " " + customer.getLastName() + ",</p>" +
                                "<p>Your transfer of " + banking.getAmount() + " RWF to account " + banking.getReceiptAccount() +
                                " has been completed successfully. Your new balance is " + customer.getBalance() + " RWF .</p>" +
                                "<img src='" + imageURL + "' alt='Image' />" +
                                "<br><br><br><br><br>" +
                                "<p>Thank you for banking with us. </p>" +
                                "<p>Regards, </p>" +
                                "<p>Banking Management System - Cedrick</p>" +
                                "</body>" +
                                "</html>";
                try {
                    emailService.sendHtmlEmail(customer.getEmail(), "Transfer Successful", emailContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                String messageReceiptEmailContent = "Dear " + receiptCustomer.getFirstName() + " " + receiptCustomer.getLastName() +
                        ",\n\nYou have received a transfer of " + bankingDTO.getAmount() + " from account " + bankingDTO.getReceiptAccount() + ". Your new balance " + receiptCustomer.getBalance();
                String receiptEmailContent = "<html>" +
                        "<body>" +
                        "<p>Dear " + receiptCustomer.getFirstName() + " " + receiptCustomer.getLastName() + ",</p>" +
                        "<p>Your have received a transfer of " + bankingDTO.getAmount() + " RWF from account " + bankingDTO.getReceiptAccount() +
                        " has been completed successfully. Your new balance is " + receiptCustomer.getBalance() + " RWF .</p>" +
                        "<img src='" + imageURL + "' alt='Image' />" +
                        "<br><br><br><br><br>" +
                        "<p>Thank you for banking with us. </p>" +
                        "<p>Regards, </p>" +
                        "<p>Banking Management System - Cedrick</p>" +
                        "</body>" +
                        "</html>";

                try {
                    emailService.sendHtmlEmail(receiptCustomer.getEmail(), "Transfer Successful", receiptEmailContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Register transaction message
                Message message = new Message();
                message.setMessage(messageEmailContent);
                message.setCustomer(customer);
                messageService.save(message);

                Message message2 = new Message();
                message2.setMessage(messageReceiptEmailContent);
                message2.setCustomer(receiptCustomer);
                messageService.save(message2);

                return savedBanking;
            } else {
                throw new IllegalArgumentException("Insufficient balance");
            }
        } else {
            throw new IllegalArgumentException("Customer or receipt customer not found");
        }
    }

    @Override
    @Transactional
    public Banking update(UUID id, BankingDTO bankingDTO) {
        Optional<Banking> bankingOpt = bankingRepository.findById(id);
        if (bankingOpt.isPresent()) {
            Banking existingBanking = bankingOpt.get();

            // Validate and update fields only if provided in bankingDTO
            if (bankingDTO.getType() != null && !bankingDTO.getType().equals(ETransactionType.TRANSFER)) {
                throw new IllegalArgumentException("Type must be set to Transfer");
            }
            if (bankingDTO.getAmount() != null && bankingDTO.getAmount() <= 0) {
                throw new IllegalArgumentException("Amount should be greater than zero");
            }

            Optional<Customer> customerOpt = customerRepository.findById(bankingDTO.getCustomer());
            Optional<Customer> receiptCustomerOpt = customerRepository.findById(bankingDTO.getReceiptCustomer());

            if (customerOpt.isPresent() && receiptCustomerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                Customer receiptCustomer = receiptCustomerOpt.get();

                // Validate and update account numbers if provided in DTO
                if (bankingDTO.getAccount() != null && !customer.getAccount().equals(bankingDTO.getAccount())) {
                    throw new IllegalArgumentException("Account number does not match customer account");
                }
                if (bankingDTO.getReceiptAccount() != null && !receiptCustomer.getAccount().equals(bankingDTO.getReceiptAccount())) {
                    throw new IllegalArgumentException("Receipt account number does not match receipt customer account");
                }

                // Perform transaction only if amount is provided in DTO
                if (bankingDTO.getAmount() != null) {
                    double originalAmount = existingBanking.getAmount();
                    double newAmount = bankingDTO.getAmount();

                    if (customer.getBalance() >= newAmount) {
                        // Revert previous transaction amounts
                        customer.setBalance(customer.getBalance() + originalAmount);
                        receiptCustomer.setBalance(receiptCustomer.getBalance() - originalAmount);
                        customerRepository.save(customer);
                        customerRepository.save(receiptCustomer);

                        // Deduct new amount from customer's balance
                        customer.setBalance(customer.getBalance() - newAmount);
                        customerRepository.save(customer);

                        // Add new amount to receipt customer's balance
                        receiptCustomer.setBalance(receiptCustomer.getBalance() + newAmount);
                        customerRepository.save(receiptCustomer);

                        // Update banking transaction
                        existingBanking.setAmount(newAmount);
                        if (bankingDTO.getAccount() != null) {
                            existingBanking.setAccount(bankingDTO.getAccount());
                        }
                        if (bankingDTO.getReceiptAccount() != null) {
                            existingBanking.setReceiptAccount(bankingDTO.getReceiptAccount());
                        }
                        if (bankingDTO.getType() != null) {
                            existingBanking.setType(bankingDTO.getType());
                        }
                        existingBanking.setCustomer(customer);
                        existingBanking.setReceiptCustomer(receiptCustomer);

                        Banking updatedBanking = bankingRepository.save(existingBanking);

                        // Send email notifications if amount was updated
                        String emailContent = "Dear " + customer.getFirstName() + " " + customer.getLastName() +
                                ",\n\nYour transfer of " + newAmount + " to account " + bankingDTO.getReceiptAccount() +
                                " has been updated successfully.";
                        emailService.sendSimpleEmail(customer.getEmail(), "Transfer Updated", emailContent);

                        String receiptEmailContent = "Dear " + receiptCustomer.getFirstName() + " " + receiptCustomer.getLastName() +
                                ",\n\nYou have received an updated transfer of " + bankingDTO.getAmount() + " from account " + bankingDTO.getAccount() + ".";
                        emailService.sendSimpleEmail(receiptCustomer.getEmail(), "Transfer Updated", receiptEmailContent);

                        return updatedBanking;
                    } else {
                        throw new IllegalArgumentException("Insufficient balance");
                    }
                } else {
                    throw new IllegalArgumentException("Customer or receipt customer not found");
                }
            } else {
                throw new IllegalArgumentException("Banking transaction not found");
            }
        }
        return null;
    }


    @Override
    public Optional<Banking> getById(UUID id) {
        return bankingRepository.findById(id);
    }

    @Override
    public boolean delete(UUID id) {
        try {
            bankingRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Banking> getAll() {
        return bankingRepository.findAll();
    }

}
