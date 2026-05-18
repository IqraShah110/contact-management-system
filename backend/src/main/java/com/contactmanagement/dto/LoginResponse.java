package com.contactmanagement.dto;

import com.contactmanagement.entity.User;

public class LoginResponse {

    private static final String DEFAULT_MESSAGE = "Login successful";

    private final String message;
    private final Long userId;
    private final String fullName;
    private final String email;
    private final String phoneNumber;

    public LoginResponse(String message, Long userId, String fullName, String email, String phoneNumber) {
        this.message = message;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static LoginResponse forUser(User user) {
        return new LoginResponse(DEFAULT_MESSAGE, user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber());
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
