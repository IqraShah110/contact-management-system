package com.contactmanagement.service;

import com.contactmanagement.dto.ChangePasswordRequest;
import com.contactmanagement.dto.LoginRequest;
import com.contactmanagement.dto.LoginResponse;
import com.contactmanagement.dto.RegisterRequest;
import com.contactmanagement.entity.User;
import com.contactmanagement.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        String email = normalize(request.getEmail());
        String phoneNumber = normalize(request.getPhoneNumber());

        if (!StringUtils.hasText(email) && !StringUtils.hasText(phoneNumber)) {
            throw new IllegalArgumentException("Either email or phone number is required");
        }

        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        if (StringUtils.hasText(phoneNumber) && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        String identifier = normalize(request.getIdentifier());
        if (!StringUtils.hasText(identifier)) {
            throw new IllegalArgumentException("Email or phone number is required");
        }

        User authenticatedUser = findByIdentifier(identifier);

        if (!passwordEncoder.matches(request.getPassword(), authenticatedUser.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return new LoginResponse(
                "Login successful",
                authenticatedUser.getId(),
                authenticatedUser.getFullName(),
                authenticatedUser.getEmail(),
                authenticatedUser.getPhoneNumber()
        );
    }

    public void changePassword(ChangePasswordRequest request) {
        String identifier = normalize(request.getIdentifier());
        if (!StringUtils.hasText(identifier)) {
            throw new IllegalArgumentException("Email or phone number is required");
        }

        User user = findByIdentifier(identifier);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findByIdentifier(String identifier) {
        Optional<User> user = userRepository.findByEmail(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByPhoneNumber(identifier);
        }
        return user.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
