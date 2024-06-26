package com.example.demo.v1.services.impl;
import com.example.demo.v1.configs.EmailService;
import com.example.demo.v1.dtos.structured.MessageDTO;
import com.example.demo.v1.dtos.structured.WithdrawDTO;
import com.example.demo.v1.enumerations.ETransactionType;
import com.example.demo.v1.models.Customer;
import com.example.demo.v1.models.Message;
import com.example.demo.v1.models.Withdraw;
import com.example.demo.v1.repositories.ICustomerRepository;
import com.example.demo.v1.repositories.IWithdrawRepository;
import com.example.demo.v1.services.IMessageService;
import com.example.demo.v1.services.IWithdrawService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WithdrawServiceImpl implements IWithdrawService {
    @Autowired
    private IWithdrawRepository withdrawRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public Withdraw save(WithdrawDTO withdrawDTO) {
        if (withdrawDTO.getType() != null && !withdrawDTO.getType().equals(ETransactionType.WITHDRAW)) {
            throw new IllegalArgumentException("Type must be set to Withdraw");
        }
        if (withdrawDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount should be greater than zero");
        }
        Withdraw withdraw = modelMapper.map(withdrawDTO, Withdraw.class);
        Optional<Customer> customerOpt = customerRepository.findById(withdrawDTO.getCustomer());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (customer.getBalance() < withdrawDTO.getAmount()) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            if (customer.getAccount().equals(withdraw.getAccount())) {
                customer.setBalance(customer.getBalance() - withdraw.getAmount());
                customerRepository.save(customer);
                withdraw.setCustomer(customer);
                Withdraw saved = withdrawRepository.save(withdraw);
                String messageContent = "Dear " + customer.getFirstName() + " " + customer.getLastName() +  " ,\n\nYour withdrawal of " + withdraw.getAmount() + " on your account " + withdraw.getAccount() + " has been completed successfully";
                String imageURL = "https://i.postimg.cc/WzW9K5Mq/Screenshot-2024-06-25-at-23-00-42.png";
                String emailContent = "<html>" +
                        "<body>" +
                        "<p>Dear " + customer.getFirstName() + " " + customer.getLastName() + ",</p>" +
                        "<p>Your withdrawal of " + withdraw.getAmount() + "RWF on your account " + withdraw.getAccount() +
                        " has been completed successfully. Your new balance is " + customer.getBalance() + "RWF .</p>" +
                        "<img src='" + imageURL + "' alt='Image' />" +
                        "<br><br><br><br><br>" +
                        "<p>Thank you for banking with us. </p>" +
                        "<p>Regards, </p>" +
                        "<p>Banking Management System - Cedrick</p>" +
                        "</body>" +
                        "</html>";
                try {
                    emailService.sendHtmlEmail(customer.getEmail(), "Withdrawal Successful", emailContent);
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
    public Withdraw update(UUID id, WithdrawDTO withdrawDTO) {
        Optional<Withdraw> withdrawOpt = withdrawRepository.findById(id);
        if (withdrawOpt.isPresent()) {
            Withdraw existingWithdraw = withdrawOpt.get();
            // Only update fields if provided in withdrawDTO
            if (withdrawDTO.getType() != null && withdrawDTO.getType() != ETransactionType.WITHDRAW) {
                throw new IllegalArgumentException("Type must be set to Withdraw");
            }
            if (withdrawDTO.getAmount() != null && withdrawDTO.getAmount() <= 0) {
                throw new IllegalArgumentException("Amount should be greater than zero");
            }
            // Update account if provided in DTO
            if (withdrawDTO.getAccount() != null) {
                existingWithdraw.setAccount(withdrawDTO.getAccount());
            }
            // Update amount if provided in DTO
            if (withdrawDTO.getAmount() != null) {
                double originalAmount = existingWithdraw.getAmount();
                double newAmount = withdrawDTO.getAmount();

                Optional<Customer> customerOpt = customerRepository.findById(withdrawDTO.getCustomer());
                if (customerOpt.isPresent()) {
                    Customer customer = customerOpt.get();
                    double balanceAfterWithdraw = customer.getBalance() + originalAmount - newAmount;
                    if (balanceAfterWithdraw < 0) {
                        throw new IllegalArgumentException("Insufficient funds");
                    }
                    customer.setBalance(customer.getBalance() - originalAmount + newAmount);
                    customerRepository.save(customer);
                    existingWithdraw.setAmount(newAmount);
                } else {
                    throw new IllegalArgumentException("Customer not found");
                }
            }
            // Update type if provided in DTO
            if (withdrawDTO.getType() != null) {
                existingWithdraw.setType(withdrawDTO.getType());
            }
            return withdrawRepository.save(existingWithdraw);
        } else {
            throw new IllegalArgumentException("Withdraw not found");
        }
    }


    @Override
    public Optional<Withdraw> getById(UUID id) {
        return withdrawRepository.findById(id);
    }

    @Override
    public boolean delete(UUID id) {
        try {
            withdrawRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Withdraw> getAll() {
        return withdrawRepository.findAll();
    }
}
