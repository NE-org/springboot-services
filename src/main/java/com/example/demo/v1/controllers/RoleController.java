package com.example.demo.v1.controllers;
import com.example.demo.v1.dtos.structured.RoleDTO;
import com.example.demo.v1.models.Role;
import com.example.demo.v1.services.IRoleService;
import com.example.demo.v1.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/generate-roles")
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    @Autowired
    private final IRoleService roleService;
    @PostMapping
    public ApiResponse<Role> save(
            @Valid @RequestBody RoleDTO roleDTO
    ){
        Role savedRole = roleService.save(roleDTO);
        if (savedRole != null){
            return new ApiResponse<>(savedRole,"Successfully Registered",HttpStatus.CREATED,true);
        }
        return new ApiResponse<>("Failed to save role", (Object) null, HttpStatus.BAD_REQUEST);
    }
    @GetMapping
    public ApiResponse<List<Role>> getAll() {
        List<Role> roles = roleService.getAll();
        if (roles != null){
            return new ApiResponse<>(roles,"Successful Retrieval",HttpStatus.OK,true);
        }
        return new ApiResponse<>("Failure to retrieve",(Object) null,HttpStatus.BAD_REQUEST);
    }
    @GetMapping(value = "/{id}")
    public ApiResponse<Optional<Role>> getById(@PathVariable(value = "id") UUID id){
        Optional<Role> role = roleService.getById(id);
        if (role.isPresent()) {
            return new ApiResponse<>(role,"Successful Role retrieved !",HttpStatus.OK,true);
        }
        return new ApiResponse<>("Failure to retrieve single role",(Object) null,HttpStatus.BAD_REQUEST);
    }
    @DeleteMapping(value = "/{id}")
    public ApiResponse<Boolean> remove(@PathVariable(value = "id") UUID id){
        boolean deletionSuccessful = roleService.delete(id);
        if(deletionSuccessful){
            return new ApiResponse<>(true,"Role Successfully delete!",HttpStatus.OK,true);
        }
        return new ApiResponse<>("Failure to delete role",(Object) null,HttpStatus.BAD_REQUEST);
    }
    @PutMapping(value = "/{id}")
    public ApiResponse<Role> update(
            @PathVariable(value = "id") UUID id,
            @Valid @RequestBody RoleDTO roleDTO
    ) throws RoleNotFoundException {
        Role updatedRole = roleService.update(id,roleDTO);
        return new ApiResponse<>(updatedRole,"Role Successfully updated!",HttpStatus.OK,true);
    }
}
