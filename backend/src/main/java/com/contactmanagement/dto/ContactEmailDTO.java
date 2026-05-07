package com.contactmanagement.dto;

public class ContactEmailDTO {

    private Long id;
    private String email;
    private String label;

    public ContactEmailDTO() {
    }

    public ContactEmailDTO(String email, String label) {
        this.email = email;
        this.label = label;
    }

    public ContactEmailDTO(Long id, String email, String label) {
        this.id = id;
        this.email = email;
        this.label = label;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
