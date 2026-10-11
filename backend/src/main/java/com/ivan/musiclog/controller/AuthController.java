package com.ivan.musiclog.controller;

import com.ivan.musiclog.dto.LoginRequest;
import com.ivan.musiclog.dto.LoginResponse;
import com.ivan.musiclog.dto.RegisterRequest;
import com.ivan.musiclog.dto.RegisterResponse;
import com.ivan.musiclog.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegisterResponse registerUser(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}