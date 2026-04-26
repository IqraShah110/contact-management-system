package com.contactmanagement.dto;

public class LoginResponse {

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
