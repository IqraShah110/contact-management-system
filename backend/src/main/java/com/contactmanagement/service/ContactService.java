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
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.repository.ContactEmailRepository;
import com.contactmanagement.repository.ContactPhoneRepository;
import com.contactmanagement.repository.ContactRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

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
                            parseEmailLabel(emailDTO.getLabel())))
                    .collect(Collectors.toList());
            contactEmailRepository.saveAll(emails);
            savedContact.setEmails(emails);
        }

        // Add phones if provided
        if (request.getPhones() != null && !request.getPhones().isEmpty()) {
            List<ContactPhone> phones = request.getPhones().stream()
                    .map(phoneDTO -> new ContactPhone(savedContact, phoneDTO.getPhoneNumber(),
                            parsePhoneLabel(phoneDTO.getLabel())))
                    .collect(Collectors.toList());
            contactPhoneRepository.saveAll(phones);
            savedContact.setPhones(phones);
        }

        log.info("Contact created id={} for user id={}", savedContact.getId(), user.getId());
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
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

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
                            parseEmailLabel(emailDTO.getLabel())))
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
                            parsePhoneLabel(phoneDTO.getLabel())))
                    .collect(Collectors.toList());
            contact.setPhones(contactPhoneRepository.saveAll(newPhones));
        }

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact updated id={} for user id={}", contactId, user.getId());
        return mapToResponse(updatedContact);
    }

    @Transactional
    public void deleteContact(Long contactId, User user) {
        if (!contactRepository.existsByIdAndUser(contactId, user)) {
            throw new ResourceNotFoundException("Contact not found");
        }
        contactRepository.deleteByIdAndUser(contactId, user);
        log.info("Contact deleted id={} for user id={}", contactId, user.getId());
    }

    private static ContactEmail.EmailLabel parseEmailLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Email label is required (work, personal, or other)");
        }
        try {
            return ContactEmail.EmailLabel.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid email label: use work, personal, or other", ex);
        }
    }

    private static ContactPhone.PhoneLabel parsePhoneLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Phone label is required (work, home, personal, or other)");
        }
        try {
            return ContactPhone.PhoneLabel.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid phone label: use work, home, personal, or other", ex);
        }
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
