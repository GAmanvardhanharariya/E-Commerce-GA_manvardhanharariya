package com.ecommerce.demo.auth.service;

import com.ecommerce.demo.auth.dto.*;
import com.ecommerce.demo.auth.entity.AppUser;
import com.ecommerce.demo.auth.entity.Role;
import com.ecommerce.demo.auth.repository.UserRepository;
import com.ecommerce.demo.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repo, PasswordEncoder encoder, JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public TokenResponse register(RegisterRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        AppUser user = AppUser.builder()
                .name(req.name())
                .email(req.email())
                .passwordHash(encoder.encode(req.password()))
                .role(Role.CUSTOMER)
                .build();

        repo.save(user);
        return new TokenResponse(jwtService.generateToken(user), "Bearer");
    }

    public TokenResponse login(LoginRequest req) {
        AppUser user = repo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return new TokenResponse(jwtService.generateToken(user), "Bearer");
    }
}
