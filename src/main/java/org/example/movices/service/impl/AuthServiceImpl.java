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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername().toLowerCase());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> assignedRoles = new HashSet<>();

        // If no roles specified, assign default ROLE_USER
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            request.getRole().forEach(roleName -> {

                Role role = roleRepository.findByName(RoleType.valueOf("ROLE_" + roleName.toUpperCase()))
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                assignedRoles.add(role);
            });
        } else {
            // default ROLE_USER
            Role defaultRole = roleRepository.findByName(RoleType.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not configured"));
            assignedRoles.add(defaultRole);
        }


        user.setRoles(assignedRoles);
        return userRepository.save(user);
    }

    @Override
    public JwtResponse signIn(LoginRequest request) {
        // Find user by username
        User userEntity = userRepository.findByUsername(request.getUsername().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Get role names
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
