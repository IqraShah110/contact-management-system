package com.contactmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.contactmanagement.entity.User;
import com.contactmanagement.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameFindsByEmail() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPasswordHash("hash");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("alice@example.com");

        assertEquals("alice@example.com", details.getUsername());
        assertEquals("hash", details.getPassword());
    }

    @Test
    void loadUserByUsernameFindsByPhoneWhenEmailMissing() {
        User user = new User();
        user.setPhoneNumber("03001234567");
        user.setPasswordHash("hash");
        when(userRepository.findByEmail("03001234567")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("03001234567")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("03001234567");

        assertEquals("03001234567", details.getUsername());
    }

    @Test
    void loadUserByUsernameThrowsWhenNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown@example.com"));
    }
}
