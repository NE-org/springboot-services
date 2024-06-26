package com.example.demo.v1.services;
import com.example.demo.v1.dtos.structured.RoleDTO;
import com.example.demo.v1.models.Role;
import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRoleService {
    Role save(RoleDTO roleDTO);
    Role update(UUID id, RoleDTO roleDTO) throws RoleNotFoundException;
    Optional<Role> getById(UUID id);
    List<Role> getAll();
    boolean delete(UUID id);
}
