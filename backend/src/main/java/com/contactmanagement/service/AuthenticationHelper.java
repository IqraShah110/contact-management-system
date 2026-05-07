package com.contactmanagement.service;

import com.contactmanagement.entity.User;
import com.contactmanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationHelper {

    private final UserRepository userRepository;

    public AuthenticationHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhoneNumber(username))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database"));
    }
}
