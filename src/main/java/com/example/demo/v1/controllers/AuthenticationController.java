package com.example.demo.v1.controllers;
import com.example.demo.v1.dtos.structured.SignInDTO;
import com.example.demo.v1.dtos.structured.UserDTO;
import com.example.demo.v1.models.User;
import com.example.demo.v1.services.impl.AuthenticationService;
import com.example.demo.v1.utils.ApiResponse;
import com.example.demo.v1.utils.JWTAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.management.relation.RoleNotFoundException;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("/register")
    public ApiResponse<User> register(
            @RequestBody UserDTO userDTO
    ) throws RoleNotFoundException {
        User savedUSer = service.register(userDTO);
        if (savedUSer != null) {
            return new ApiResponse<>(savedUSer,"User Registered successfully", HttpStatus.CREATED,true);
        }
        return new ApiResponse<>("Failure to record",null,HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/login")
    public ResponseEntity<JWTAuthenticationResponse> authenticate(
            @RequestBody SignInDTO request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
