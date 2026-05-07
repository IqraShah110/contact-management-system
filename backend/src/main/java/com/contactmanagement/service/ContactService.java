package com.contactmanagement.service;

import com.contactmanagement.dto.ContactEmailDTO;
import com.contactmanagement.dto.ContactPhoneDTO;
import com.contactmanagement.dto.ContactResponse;
import com.contactmanagement.dto.CreateContactRequest;
import com.contactmanagement.dto.UpdateContactRequest;
import com.contactmanagement.entity.Contact;
import com.contactmanagement.entity.ContactEmail;
import com.contactmanagement.entity.ContactPhone;
import com.contactmanagement.entity.User;
import com.contactmanagement.repository.ContactEmailRepository;
import com.contactmanagement.repository.ContactPhoneRepository;
import com.contactmanagement.repository.ContactRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactEmailRepository contactEmailRepository;
    private final ContactPhoneRepository contactPhoneRepository;

    public ContactService(ContactRepository contactRepository,
                         ContactEmailRepository contactEmailRepository,
                         ContactPhoneRepository contactPhoneRepository) {
        this.contactRepository = contactRepository;
        this.contactEmailRepository = contactEmailRepository;
        this.contactPhoneRepository = contactPhoneRepository;
    }

    @Transactional
    public ContactResponse createContact(User user, CreateContactRequest request) {
        if (!StringUtils.hasText(request.getFirstName()) || !StringUtils.hasText(request.getLastName())) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        Contact contact = new Contact(user, request.getFirstName().trim(), 
                                     request.getLastName().trim(), 
                                     request.getTitle() != null ? request.getTitle().trim() : null);
        
        Contact savedContact = contactRepository.save(contact);

        // Add emails if provided
        if (request.getEmails() != null && !request.getEmails().isEmpty()) {
            List<ContactEmail> emails = request.getEmails().stream()
                    .map(emailDTO -> new ContactEmail(savedContact, emailDTO.getEmail(), 
                                                     ContactEmail.EmailLabel.valueOf(emailDTO.getLabel().toUpperCase())))
                    .collect(Collectors.toList());
            contactEmailRepository.saveAll(emails);
            savedContact.setEmails(emails);
        }

        // Add phones if provided
        if (request.getPhones() != null && !request.getPhones().isEmpty()) {
            List<ContactPhone> phones = request.getPhones().stream()
                    .map(phoneDTO -> new ContactPhone(savedContact, phoneDTO.getPhoneNumber(),
                                                     ContactPhone.PhoneLabel.valueOf(phoneDTO.getLabel().toUpperCase())))
                    .collect(Collectors.toList());
            contactPhoneRepository.saveAll(phones);
            savedContact.setPhones(phones);
        }

        return mapToResponse(savedContact);
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> getAllContacts(User user, Pageable pageable) {
        Page<Contact> contacts = contactRepository.findByUser(user, pageable);
        return contacts.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long contactId, User user) {
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        return mapToResponse(contact);
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> searchContacts(User user, String searchTerm, Pageable pageable) {
        if (!StringUtils.hasText(searchTerm)) {
            return getAllContacts(user, pageable);
        }
        
        Page<Contact> contacts = contactRepository.searchByFirstNameOrLastName(user, searchTerm.trim(), pageable);
        return contacts.map(this::mapToResponse);
    }

    @Transactional
    public ContactResponse updateContact(Long contactId, User user, UpdateContactRequest request) {
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        if (!StringUtils.hasText(request.getFirstName()) || !StringUtils.hasText(request.getLastName())) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        contact.setFirstName(request.getFirstName().trim());
        contact.setLastName(request.getLastName().trim());
        contact.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        contact.setUpdatedAt(LocalDateTime.now());

        // Update emails
        if (request.getEmails() != null) {
            // Remove old emails
            contactEmailRepository.deleteAll(contact.getEmails());
            
            // Add new emails
            List<ContactEmail> newEmails = request.getEmails().stream()
                    .map(emailDTO -> new ContactEmail(contact, emailDTO.getEmail(),
                                                     ContactEmail.EmailLabel.valueOf(emailDTO.getLabel().toUpperCase())))
                    .collect(Collectors.toList());
            contact.setEmails(contactEmailRepository.saveAll(newEmails));
        }

        // Update phones
        if (request.getPhones() != null) {
            // Remove old phones
            contactPhoneRepository.deleteAll(contact.getPhones());
            
            // Add new phones
            List<ContactPhone> newPhones = request.getPhones().stream()
                    .map(phoneDTO -> new ContactPhone(contact, phoneDTO.getPhoneNumber(),
                                                     ContactPhone.PhoneLabel.valueOf(phoneDTO.getLabel().toUpperCase())))
                    .collect(Collectors.toList());
            contact.setPhones(contactPhoneRepository.saveAll(newPhones));
        }

        Contact updatedContact = contactRepository.save(contact);
        return mapToResponse(updatedContact);
    }

    @Transactional
    public void deleteContact(Long contactId, User user) {
        if (!contactRepository.existsByIdAndUser(contactId, user)) {
            throw new IllegalArgumentException("Contact not found");
        }
        contactRepository.deleteByIdAndUser(contactId, user);
    }

    private ContactResponse mapToResponse(Contact contact) {
        List<ContactEmailDTO> emailDTOs = contact.getEmails() != null ? 
            contact.getEmails().stream()
                .map(email -> new ContactEmailDTO(email.getId(), email.getEmail(), email.getLabel().toString()))
                .collect(Collectors.toList()) : List.of();

        List<ContactPhoneDTO> phoneDTOs = contact.getPhones() != null ? 
            contact.getPhones().stream()
                .map(phone -> new ContactPhoneDTO(phone.getId(), phone.getPhoneNumber(), phone.getLabel().toString()))
                .collect(Collectors.toList()) : List.of();

        return new ContactResponse(contact.getId(), contact.getFirstName(), contact.getLastName(),
                                  contact.getTitle(), emailDTOs, phoneDTOs, 
                                  contact.getCreatedAt(), contact.getUpdatedAt());
    }
}
