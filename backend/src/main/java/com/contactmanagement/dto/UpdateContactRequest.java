package com.contactmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class UpdateContactRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String title;

    private List<ContactEmailDTO> emails;

    private List<ContactPhoneDTO> phones;

    public UpdateContactRequest() {
        // Required for JSON deserialization.
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ContactEmailDTO> getEmails() {
        return emails;
    }

    public void setEmails(List<ContactEmailDTO> emails) {
        this.emails = emails;
    }

    public List<ContactPhoneDTO> getPhones() {
        return phones;
    }

    public void setPhones(List<ContactPhoneDTO> phones) {
        this.phones = phones;
    }
}
