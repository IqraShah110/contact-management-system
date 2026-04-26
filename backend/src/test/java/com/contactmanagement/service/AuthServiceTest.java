package com.contactmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.contactmanagement.dto.ChangePasswordRequest;
import com.contactmanagement.dto.LoginRequest;
import com.contactmanagement.dto.LoginResponse;
import com.contactmanagement.dto.RegisterRequest;
import com.contactmanagement.entity.User;
import com.contactmanagement.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldSaveUserWhenEmailIsValid() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ali Khan");
        request.setEmail("ali@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmail("ali@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedPassword");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("Ali Khan", savedUser.getFullName());
        assertEquals("ali@example.com", savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void registerShouldThrowWhenEmailAndPhoneAreMissing() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ali Khan");
        request.setPassword("secret123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Either email or phone number is required", ex.getMessage());
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ali Khan");
        request.setEmail("ali@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmail("ali@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Email is already registered", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowWhenPhoneAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ali Khan");
        request.setPhoneNumber("03001234567");
        request.setPassword("secret123");

        when(userRepository.existsByPhoneNumber("03001234567")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Phone number is already registered", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldReturnResponseWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("ali@example.com");
        request.setPassword("secret123");

        User user = new User();
        user.setEmail("ali@example.com");
        user.setFullName("Ali Khan");
        user.setPasswordHash("hashedPassword");
        user.setPhoneNumber("03001234567");

        when(userRepository.findByEmail("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashedPassword")).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertEquals("Login successful", response.getMessage());
        assertEquals("Ali Khan", response.getFullName());
        assertEquals("ali@example.com", response.getEmail());
        assertEquals("03001234567", response.getPhoneNumber());
    }

    @Test
    void loginShouldThrowWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("ali@example.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByEmail("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void loginShouldThrowWhenIdentifierIsBlank() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("  ");
        request.setPassword("secret123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        assertEquals("Email or phone number is required", ex.getMessage());
    }

    @Test
    void changePasswordShouldUpdatePasswordWhenCurrentPasswordIsValid() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setIdentifier("ali@example.com");
        request.setCurrentPassword("old123");
        request.setNewPassword("new123");

        User user = new User();
        user.setPasswordHash("oldHash");

        when(userRepository.findByEmail("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old123", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("new123")).thenReturn("newHash");

        authService.changePassword(request);

        verify(userRepository).save(any(User.class));
        assertEquals("newHash", user.getPasswordHash());
    }

    @Test
    void changePasswordShouldThrowWhenCurrentPasswordIsIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setIdentifier("ali@example.com");
        request.setCurrentPassword("wrongOld");
        request.setNewPassword("new123");

        User user = new User();
        user.setPasswordHash("oldHash");

        when(userRepository.findByEmail("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "oldHash")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(request)
        );
        assertEquals("Current password is incorrect", ex.getMessage());
    }

    @Test
    void changePasswordShouldThrowWhenNewPasswordMatchesCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setIdentifier("ali@example.com");
        request.setCurrentPassword("samePass123");
        request.setNewPassword("samePass123");

        User user = new User();
        user.setPasswordHash("oldHash");

        when(userRepository.findByEmail("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("samePass123", "oldHash")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(request)
        );
        assertEquals("New password must be different from current password", ex.getMessage());
    }
}
