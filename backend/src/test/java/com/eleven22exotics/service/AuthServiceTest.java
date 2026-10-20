package com.eleven22exotics.service;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.entity.User;
import com.eleven22exotics.exception.ApiException;
import com.eleven22exotics.repository.UserRepository;
import com.eleven22exotics.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    final JwtService jwt = new JwtService("unit-test-secret-unit-test-secret-1234", 60_000);

    private AuthService service() { return new AuthService(users, encoder, jwt); }

    @Test void registersUserWithHashedPassword() {
        when(users.existsByEmail("new@user.dk")).thenReturn(false);
        AuthResponse r = service().register(new RegisterRequest("New", "User", "New@User.dk", "Password123", null));
        assertNotNull(r.token());
        assertEquals("new@user.dk", r.user().email());
        verify(users).save(argThat(u -> !u.getPasswordHash().equals("Password123") && u.getPasswordHash().startsWith("$2")));
    }

    @Test void rejectsDuplicateEmail() {
        when(users.existsByEmail("dup@user.dk")).thenReturn(true);
        ApiException e = assertThrows(ApiException.class,
                () -> service().register(new RegisterRequest("A", "B", "dup@user.dk", "Password123", null)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test void loginSucceedsWithCorrectPasswordOnly() {
        User u = new User();
        u.setEmail("a@b.dk");
        u.setFirstName("A");
        u.setLastName("B");
        u.setPasswordHash(encoder.encode("Password123"));
        when(users.findByEmail("a@b.dk")).thenReturn(Optional.of(u));
        assertNotNull(service().login(new LoginRequest("a@b.dk", "Password123")).token());
        ApiException e = assertThrows(ApiException.class, () -> service().login(new LoginRequest("a@b.dk", "wrong")));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }
}
