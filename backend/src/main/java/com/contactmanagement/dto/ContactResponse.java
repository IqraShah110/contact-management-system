package com.contactmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ContactResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private List<ContactEmailDTO> emails;
    private List<ContactPhoneDTO> phones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContactResponse() {
    }

    public ContactResponse(Long id, String firstName, String lastName, String title,
                          List<ContactEmailDTO> emails, List<ContactPhoneDTO> phones,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.emails = emails;
        this.phones = phones;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
