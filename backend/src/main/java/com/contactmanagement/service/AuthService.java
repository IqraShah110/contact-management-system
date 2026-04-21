package com.contactmanagement.service;

import com.contactmanagement.dto.RegisterRequest;
import com.contactmanagement.entity.User;
import com.contactmanagement.repository.UserRepository;
import java.time.LocalDateTime;
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

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
