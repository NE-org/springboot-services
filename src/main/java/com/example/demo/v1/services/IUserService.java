package com.example.demo.v1.services;
import com.example.demo.v1.dtos.structured.UserDTO;
import com.example.demo.v1.models.User;
import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserService {
    User save(UserDTO userDTO) throws RoleNotFoundException;
    User update(UUID id, UserDTO userDTO) throws RoleNotFoundException;
    Optional<User> getById(UUID id);
    List<User> getAll();
    boolean delete(UUID id);
}
