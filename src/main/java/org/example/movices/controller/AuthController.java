package org.example.movices.controller;

import lombok.RequiredArgsConstructor;
import org.example.movices.dto.request.LoginRequest;
import org.example.movices.dto.request.SignupRequest;
import org.example.movices.dto.response.JwtResponse;
import org.example.movices.model.entity.User;
import org.example.movices.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody SignupRequest request) {
        User user = authService.signUp(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.signIn(request);
        return ResponseEntity.ok(jwtResponse);
    }
}