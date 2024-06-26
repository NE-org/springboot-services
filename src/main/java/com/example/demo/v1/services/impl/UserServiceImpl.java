package com.example.demo.v1.services.impl;
import com.example.demo.v1.models.Role;
import com.example.demo.v1.repositories.IRoleRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import com.example.demo.v1.dtos.structured.UserDTO;
import com.example.demo.v1.models.User;
import com.example.demo.v1.repositories.IUserRepository;
import com.example.demo.v1.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapperConfig;
    @Override
    public User save(UserDTO userDTO) throws RoleNotFoundException {
        User user = modelMapperConfig.map(userDTO,User.class);
        System.out.println(user);
        Role role = roleRepository.findById(userDTO.getRole().getId())
                .orElseThrow(() -> new RoleNotFoundException("Role not found for id: " + userDTO.getRole().getId()));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public User update(UUID id, UserDTO userDTO) throws RoleNotFoundException {
        Optional<User> findUser = userRepository.findById(id);
        if (findUser.isPresent()){
            User existingUser = findUser.get();
            existingUser.setEmail(userDTO.getEmail());
            existingUser.setFirstName(userDTO.getFirstName());
            existingUser.setLastName(userDTO.getLastName());
            return userRepository.save(existingUser);
        } else {
            throw new RoleNotFoundException("User not found !");
        }
    }

    @Override
    public Optional<User> getById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        try {
            userRepository.deleteById(id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
