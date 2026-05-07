package com.contactmanagement.dto;

public class ContactPhoneDTO {

    private Long id;
    private String phoneNumber;
    private String label;

    public ContactPhoneDTO() {
    }

    public ContactPhoneDTO(String phoneNumber, String label) {
        this.phoneNumber = phoneNumber;
        this.label = label;
    }

    public ContactPhoneDTO(Long id, String phoneNumber, String label) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.label = label;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
