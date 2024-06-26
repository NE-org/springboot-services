package com.example.demo.v1.controllers;
import com.example.demo.v1.dtos.structured.UserDTO;
import com.example.demo.v1.models.User;
import com.example.demo.v1.services.IUserService;
import com.example.demo.v1.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    @GetMapping(value = "/welcome")
    public ResponseEntity<String> welcomeTestMessage(){
        return ResponseEntity.status(HttpStatus.OK).body("Welcome to the Platform");
    }
    @PostMapping
    public ApiResponse<User> save(
            @Valid @RequestBody UserDTO userDTO
    ) throws RoleNotFoundException {
        User savedUser = userService.save(userDTO);
        if (savedUser != null){
            return new ApiResponse<>(savedUser,"User Registered successfully",HttpStatus.CREATED,true);
        }
        return new ApiResponse<>("Failure to add User",null,HttpStatus.BAD_REQUEST);
    }
    @GetMapping
    public ApiResponse<List<User>> getAll() {
        List<User> users = userService.getAll();
        if (users != null){
            return new ApiResponse<>(users,"Users retrieval successful",HttpStatus.OK,true);
        }
        return new ApiResponse<>("Failure User retrieval",null,HttpStatus.BAD_REQUEST);
    }
    @GetMapping(value = "/{id}")
    public ApiResponse<Optional<User>> getById(@PathVariable(value = "id") UUID id){
        Optional<User> user = userService.getById(id);
        if (user.isPresent()){
            return new ApiResponse<>(user,"User retrieval Successful!",HttpStatus.OK,true);
        }
        return new ApiResponse<>("Failure to retrieve user",null,HttpStatus.BAD_REQUEST);
    }
    @DeleteMapping(value = "/{id}")
    public ApiResponse<Boolean> remove(@PathVariable(value = "id") UUID id){
        boolean deletionSuccessful = userService.delete(id);
        if(deletionSuccessful){
            return new ApiResponse<>(true,"User Successfully delete!",HttpStatus.OK,true);
        }
        return new ApiResponse<>("Failure to delete user",null,HttpStatus.BAD_REQUEST);
    }
    @PutMapping(value = "/{id}")
    public ApiResponse<User> update(
            @PathVariable(value = "id") UUID id,
            @Valid @RequestBody UserDTO userDTO
    ) throws RoleNotFoundException {
        User updatedUser = userService.update(id,userDTO);
        return new ApiResponse<>(updatedUser,"User successfully updated !",HttpStatus.OK,true);
    }
}
