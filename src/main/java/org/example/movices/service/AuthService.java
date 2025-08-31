package org.example.movices.service;

import org.example.movices.dto.request.LoginRequest;
import org.example.movices.dto.request.SignupRequest;
import org.example.movices.dto.response.JwtResponse;
import org.example.movices.model.entity.User;

public interface AuthService {
    User signUp(SignupRequest request);
    JwtResponse signIn(LoginRequest request); // Changed to return JwtResponse
}