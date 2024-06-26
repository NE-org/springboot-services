package com.example.demo.v1.services.impl;
import com.example.demo.v1.dtos.structured.SignInDTO;
import com.example.demo.v1.dtos.structured.UserDTO;
import com.example.demo.v1.models.Role;
import com.example.demo.v1.models.User;
import com.example.demo.v1.repositories.IRoleRepository;
import com.example.demo.v1.repositories.IUserRepository;
import com.example.demo.v1.security.JwtService;
import com.example.demo.v1.utils.JWTAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.management.relation.RoleNotFoundException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final IUserRepository repository;
    private final IRoleRepository roleRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private ModelMapper modelMapper;
    public User register(UserDTO userDTO) throws RoleNotFoundException {
        User user = modelMapper.map(userDTO,User.class);
        Role role = roleRepository.findById(userDTO.getRole().getId())
                .orElseThrow(() -> new RoleNotFoundException("Role not found for id: " + userDTO.getRole().getId()));
        user.setRole(role);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public JWTAuthenticationResponse authenticate(SignInDTO signInDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInDTO.getEmail(),
                        signInDTO.getPassword()
                )
        );
        var user = repository.findByEmail(signInDTO.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new JWTAuthenticationResponse(jwtToken);
    }
}
