package com.example.demo.v1.services.impl;

import com.example.demo.v1.dtos.structured.RoleDTO;
import com.example.demo.v1.enumerations.EUserRole;
import com.example.demo.v1.models.Role;
import com.example.demo.v1.repositories.IRoleRepository;
import com.example.demo.v1.services.IRoleService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoleServiceImpl implements IRoleService {
    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public Role save(RoleDTO roleDTO) {
        Role role = modelMapper.map(roleDTO,Role.class);
        return roleRepository.save(role);
    }

    @Override
    public Role update(UUID id, RoleDTO roleDTO) throws RoleNotFoundException {
        Optional<Role> findRole = roleRepository.findById(id);
        if (findRole.isPresent()){
            Role existingRole = findRole.get();
            EUserRole newRoleName = EUserRole.valueOf(roleDTO.getRoleName());
            existingRole.setRoleName(newRoleName);
            return roleRepository.save(existingRole);
        } else {
            throw new RoleNotFoundException("Role not found !");
        }
    }

    @Override
    public Optional<Role> getById(UUID id) {
        return roleRepository.findById(id);
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        try {
            roleRepository.deleteById(id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
