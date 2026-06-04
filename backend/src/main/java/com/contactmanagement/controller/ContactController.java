package com.contactmanagement.controller;

import com.contactmanagement.dto.ContactResponse;
import com.contactmanagement.dto.CreateContactRequest;
import com.contactmanagement.dto.UpdateContactRequest;
import com.contactmanagement.entity.User;
import com.contactmanagement.service.AuthenticationHelper;
import com.contactmanagement.service.ContactService;
import jakarta.validation.Valid;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;
    private final AuthenticationHelper authenticationHelper;

    public ContactController(ContactService contactService, AuthenticationHelper authenticationHelper) {
        this.contactService = contactService;
        this.authenticationHelper = authenticationHelper;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @Valid @RequestBody CreateContactRequest request,
            Authentication authentication) {
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        ContactResponse contact = contactService.createContact(user, request);
        log.info("Contact created id={} for user id={}", contact.getId(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(contact);
    }

    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<ContactResponse> contacts = contactService.getAllContacts(user, pageable);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContactById(
            @PathVariable Long id,
            Authentication authentication) {
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        ContactResponse contact = contactService.getContactById(id, user);
        return ResponseEntity.ok(contact);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ContactResponse>> searchContacts(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<ContactResponse> contacts = contactService.searchContacts(user, searchTerm, pageable);
        log.debug("Contact search userId={} term='{}' results={}", user.getId(), searchTerm, contacts.getTotalElements());
        return ResponseEntity.ok(contacts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request,
            Authentication authentication) {
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        ContactResponse contact = contactService.updateContact(id, user, request);
        log.info("Contact updated id={} for user id={}", id, user.getId());
        return ResponseEntity.ok(contact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteContact(
            @PathVariable Long id,
            Authentication authentication) {
        User user = authenticationHelper.getAuthenticatedUser(authentication);
        contactService.deleteContact(id, user);
        log.info("Contact deleted id={} for user id={}", id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Contact deleted successfully"));
    }
}
