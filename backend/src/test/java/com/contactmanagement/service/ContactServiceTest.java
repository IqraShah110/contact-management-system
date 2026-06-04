package com.contactmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.contactmanagement.dto.ContactEmailDTO;
import com.contactmanagement.dto.ContactPhoneDTO;
import com.contactmanagement.dto.ContactResponse;
import com.contactmanagement.dto.CreateContactRequest;
import com.contactmanagement.dto.UpdateContactRequest;
import com.contactmanagement.entity.Contact;
import com.contactmanagement.entity.User;
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.repository.ContactEmailRepository;
import com.contactmanagement.repository.ContactPhoneRepository;
import com.contactmanagement.repository.ContactRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Test
    void getContactByIdReturnsMappedContact() {
        Contact contact = new Contact(owner, "Alice", "Smith", "Engineer");
        contact.setId(5L);
        when(contactRepository.findByIdAndUser(5L, owner)).thenReturn(Optional.of(contact));

        var response = contactService.getContactById(5L, owner);

        assertEquals("Alice", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("Engineer", response.getTitle());
    }

    @Test
    void getAllContactsReturnsPagedResults() {
        Contact c1 = new Contact(owner, "A", "One", null);
        c1.setId(1L);
        Contact c2 = new Contact(owner, "B", "Two", null);
        c2.setId(2L);
        Page<Contact> page = new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 10), 2);
        when(contactRepository.findByUser(eq(owner), any(Pageable.class))).thenReturn(page);

        Page<ContactResponse> result = contactService.getAllContacts(owner, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals("A", result.getContent().get(0).getFirstName());
    }

    @Test
    void searchContactsDelegatesToFindAllWhenTermBlank() {
        Page<Contact> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(contactRepository.findByUser(eq(owner), any(Pageable.class))).thenReturn(page);

        Page<ContactResponse> result = contactService.searchContacts(owner, "   ", PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
        verify(contactRepository).findByUser(eq(owner), any(Pageable.class));
    }

    @Test
    void searchContactsUsesRepositoryQueryWhenTermPresent() {
        Contact contact = new Contact(owner, "Alice", "Smith", null);
        contact.setId(3L);
        Page<Contact> page = new PageImpl<>(List.of(contact), PageRequest.of(0, 10), 1);
        when(contactRepository.searchByFirstNameOrLastName(owner, "ali", PageRequest.of(0, 10)))
                .thenReturn(page);

        var result = contactService.searchContacts(owner, " ali ", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Alice", result.getContent().get(0).getFirstName());
    }

    @Test
    void updateContactReplacesFieldsAndChildRecords() {
        Contact existing = new Contact(owner, "Old", "Name", "Jr");
        existing.setId(7L);
        existing.setEmails(new ArrayList<>());
        existing.setPhones(new ArrayList<>());
        when(contactRepository.findByIdAndUser(7L, owner)).thenReturn(Optional.of(existing));
        when(contactRepository.save(existing)).thenReturn(existing);
        when(contactEmailRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contactPhoneRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateContactRequest req = new UpdateContactRequest();
        req.setFirstName("New");
        req.setLastName("Person");
        req.setTitle("CEO");
        req.setEmails(List.of(new ContactEmailDTO("new@example.com", "work")));
        req.setPhones(List.of(new ContactPhoneDTO("555-0100", "home")));

        var response = contactService.updateContact(7L, owner, req);

        assertEquals("New", response.getFirstName());
        assertEquals("Person", response.getLastName());
        assertEquals("CEO", response.getTitle());
        verify(contactEmailRepository).deleteAll(any());
        verify(contactPhoneRepository).deleteAll(any());
        verify(contactEmailRepository).saveAll(any());
        verify(contactPhoneRepository).saveAll(any());
    }

    @Test
    void updateContactThrowsNotFoundWhenMissing() {
        when(contactRepository.findByIdAndUser(99L, owner)).thenReturn(Optional.empty());

        UpdateContactRequest req = new UpdateContactRequest();
        req.setFirstName("A");
        req.setLastName("B");

        assertThrows(ResourceNotFoundException.class, () -> contactService.updateContact(99L, owner, req));
    }

    @Test
    void deleteContactRemovesWhenOwned() {
        when(contactRepository.existsByIdAndUser(4L, owner)).thenReturn(true);
        doNothing().when(contactRepository).deleteByIdAndUser(4L, owner);

        contactService.deleteContact(4L, owner);

        verify(contactRepository).deleteByIdAndUser(4L, owner);
    }

    @Test
    void deleteContactThrowsNotFoundWhenMissing() {
        when(contactRepository.existsByIdAndUser(4L, owner)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> contactService.deleteContact(4L, owner));
    }
}
