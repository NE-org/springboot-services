package com.example.demo.v1.configs;

import com.example.demo.v1.dtos.structured.CustomerDTO;
import com.example.demo.v1.dtos.structured.RoleDTO;
import com.example.demo.v1.dtos.structured.UserDTO;
import com.example.demo.v1.enumerations.EUserRole;
import com.example.demo.v1.models.Role;
import com.example.demo.v1.services.ICustomerService;
import com.example.demo.v1.services.IRoleService;
import com.example.demo.v1.services.IUserService;
import com.example.demo.v1.services.impl.AuthenticationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.management.relation.RoleNotFoundException;

@Component
public class ServiceRunner implements CommandLineRunner {
    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("ServiceRunner.run");
        createCustomerIfNotExist();
        Role admin = createRoleIfNotExist(EUserRole.ADMIN);
        createRoleIfNotExist(EUserRole.USER);
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Admin");
        userDTO.setLastName("User");
        userDTO.setEmail("admin@gmail.com");
        userDTO.setPassword("admin");
        userDTO.setRole(admin);
        createAdminUserIfNotExist(userDTO);
        System.out.println("ServiceRunner.run.done");
    }

    public void createCustomerIfNotExist() {
        CustomerDTO customer = new CustomerDTO();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setBalance(1000.0);
        customer.setDob("1990-01-01");
        customer.setEmail("cedrickmanzii01@gmail.com");
        customer.setMobile("0788687908");
        customerService.save(customer);
        System.out.println("ServiceRunner.createCustomerIfNotExist");
    }

    public Role createRoleIfNotExist(EUserRole role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setRoleName(role.name());
        System.out.println("ServiceRunner.createRoleIfNotExist");
        return roleService.save(roleDTO);
    }

    public void createAdminUserIfNotExist(UserDTO userDTO) throws RoleNotFoundException {
        authenticationService.register(userDTO);
        System.out.println("ServiceRunner.createAdminUserIfNotExist");
    }
}