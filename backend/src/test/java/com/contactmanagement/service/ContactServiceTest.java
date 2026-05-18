package com.contactmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.contactmanagement.dto.ContactEmailDTO;
import com.contactmanagement.dto.ContactPhoneDTO;
import com.contactmanagement.dto.CreateContactRequest;
import com.contactmanagement.entity.Contact;
import com.contactmanagement.entity.User;
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.repository.ContactEmailRepository;
import com.contactmanagement.repository.ContactPhoneRepository;
import com.contactmanagement.repository.ContactRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactEmailRepository contactEmailRepository;

    @Mock
    private ContactPhoneRepository contactPhoneRepository;

    @InjectMocks
    private ContactService contactService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
    }

    @Test
    void getContactByIdThrowsNotFoundWhenMissing() {
        when(contactRepository.findByIdAndUser(2L, owner)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(2L, owner));
        assertEquals("Contact not found", ex.getMessage());
    }

    @Test
    void createContactRejectsBadEmailLabel() {
        CreateContactRequest req = new CreateContactRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmails(List.of(new ContactEmailDTO("x@y.com", "nope")));

        Contact saved = new Contact(owner, "A", "B", null);
        saved.setId(9L);
        when(contactRepository.save(any(Contact.class))).thenReturn(saved);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> contactService.createContact(owner, req));
        assertEquals("Invalid email label: use work, personal, or other", ex.getMessage());
    }

    @Test
    void createContactAcceptsNormalizedLabels() {
        CreateContactRequest req = new CreateContactRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmails(List.of(new ContactEmailDTO("x@y.com", "personal")));
        req.setPhones(List.of(new ContactPhoneDTO("555", "other")));

        Contact saved = new Contact(owner, "A", "B", null);
        saved.setId(9L);
        when(contactRepository.save(any(Contact.class))).thenReturn(saved);
        when(contactEmailRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(contactPhoneRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = contactService.createContact(owner, req);
        assertEquals("A", response.getFirstName());
        assertEquals(1, response.getEmails().size());
        assertEquals(1, response.getPhones().size());
    }
}
