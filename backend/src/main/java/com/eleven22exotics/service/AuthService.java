package com.eleven22exotics.service;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.entity.User;
import com.eleven22exotics.exception.ApiException;
import com.eleven22exotics.repository.UserRepository;
import com.eleven22exotics.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        User u = new User();
        u.setFirstName(r.firstName().trim());
        u.setLastName(r.lastName().trim());
        u.setEmail(email);
        u.setPhoneNumber(r.phoneNumber());
        u.setPasswordHash(encoder.encode(r.password()));
        users.save(u);
        return new AuthResponse(jwt.generate(u), UserResponse.from(u));
    }

    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .filter(User::isEnabled)
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(jwt.generate(u), UserResponse.from(u));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        return users.findByEmail(email).map(UserResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in"));
    }
}
