package org.example.movices.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.movices.dto.request.LoginRequest;
import org.example.movices.dto.request.SignupRequest;
import org.example.movices.dto.response.JwtResponse;
import org.example.movices.exception.ResourceNotFoundException;
import org.example.movices.exception.UnauthorizedException;
import org.example.movices.model.entity.Role;
import org.example.movices.model.entity.User;
import org.example.movices.model.entity.enums.RoleType;
import org.example.movices.repository.RoleRepository;
import org.example.movices.repository.UserRepository;
import org.example.movices.service.AuthService;
import org.example.movices.config.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public User signUp(SignupRequest request) {
        // Ensure username and email are unique
        if (userRepository.existsByUsername(request.getUsername().toLowerCase())) {
            throw new UnauthorizedException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new UnauthorizedException("Email is already in use!");
        }

        // Fetch default user role
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not configured"));

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername().toLowerCase()); // store in lowercase
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // encode password
        user.getRoles().add(userRole);

        return userRepository.save(user);
    }

    @Override
    public JwtResponse signIn(LoginRequest request) {
        // Find user by username (convert to lowercase to match storage)
        User userEntity = userRepository.findByUsername(request.getUsername().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Get roles
        List<String> roles = userEntity.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        // Generate JWT
        String token = jwtTokenProvider.generateToken(userEntity);
        long expiresAt = jwtTokenProvider.getExpirationDateFromToken(token).getTime();

        return new JwtResponse(
                token,
                userEntity.getUsername(),
                roles,
                expiresAt
        );
    }
}
