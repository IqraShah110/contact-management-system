package com.contactmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.contactmanagement.dto.ChangePasswordRequest;
import com.contactmanagement.dto.RegisterRequest;
import com.contactmanagement.entity.User;
import com.contactmanagement.repository.UserRepository;
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
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User toSave = invocation.getArgument(0);
                    if (toSave.getId() == null) {
                        toSave.setId(99L);
                    }
                    return toSave;
                });

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

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Either email or phone number is required", ex.getMessage());
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Ali Khan");
        request.setEmail("ali@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmail("ali@example.com")).thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.register(request));
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

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Phone number is already registered", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePasswordShouldUpdatePasswordWhenCurrentPasswordIsValid() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old123");
        request.setNewPassword("new123");

        User user = new User();
        user.setPasswordHash("oldHash");

        when(passwordEncoder.matches("old123", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("new123")).thenReturn("newHash");

        authService.changePassword(user, request);

        verify(userRepository).save(user);
        assertEquals("newHash", user.getPasswordHash());
    }

    @Test
    void changePasswordShouldThrowWhenCurrentPasswordIsIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongOld");
        request.setNewPassword("new123");

        User user = new User();
        user.setPasswordHash("oldHash");

        when(passwordEncoder.matches("wrongOld", "oldHash")).thenReturn(false);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> authService.changePassword(user, request));
        assertEquals("Current password is incorrect", ex.getMessage());
    }

    @Test
    void changePasswordShouldThrowWhenNewPasswordMatchesCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("samePass123");
        request.setNewPassword("samePass123");

        User user = new User();
        user.setPasswordHash("oldHash");

        when(passwordEncoder.matches("samePass123", "oldHash")).thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.changePassword(user, request));
        assertEquals("New password must be different from current password", ex.getMessage());
    }
}
