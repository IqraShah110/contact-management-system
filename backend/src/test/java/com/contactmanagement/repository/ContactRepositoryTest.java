package com.contactmanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.contactmanagement.entity.Contact;
import com.contactmanagement.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = persistUser("Owner One", "owner@example.com", "03001111111");
        otherUser = persistUser("Other User", "other@example.com", "03002222222");
    }

    @Test
    void findByUserReturnsOnlyOwnersContacts() {
        saveContact(owner, "Alice", "Smith");
        saveContact(owner, "Bob", "Jones");
        saveContact(otherUser, "Carol", "White");

        Page<Contact> page = contactRepository.findByUser(owner, PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(c -> c.getUser().getId().equals(owner.getId())));
    }

    @Test
    void searchByFirstNameOrLastNameMatchesCaseInsensitive() {
        saveContact(owner, "Alice", "Smith");
        saveContact(owner, "Bob", "Anderson");
        saveContact(owner, "Charlie", "Brown");

        Page<Contact> byFirst = contactRepository.searchByFirstNameOrLastName(owner, "alice", PageRequest.of(0, 10));
        Page<Contact> byLast = contactRepository.searchByFirstNameOrLastName(owner, "ANDERSON", PageRequest.of(0, 10));

        assertEquals(1, byFirst.getTotalElements());
        assertEquals("Alice", byFirst.getContent().get(0).getFirstName());
        assertEquals(1, byLast.getTotalElements());
        assertEquals("Anderson", byLast.getContent().get(0).getLastName());
    }

    @Test
    void findByIdAndUserScopesToOwner() {
        Contact contact = saveContact(owner, "Alice", "Smith");

        assertTrue(contactRepository.findByIdAndUser(contact.getId(), owner).isPresent());
        assertFalse(contactRepository.findByIdAndUser(contact.getId(), otherUser).isPresent());
    }

    @Test
    void deleteByIdAndUserRemovesOnlyMatchingContact() {
        Contact toDelete = saveContact(owner, "Alice", "Smith");
        Contact toKeep = saveContact(owner, "Bob", "Jones");

        contactRepository.deleteByIdAndUser(toDelete.getId(), owner);

        assertFalse(contactRepository.existsByIdAndUser(toDelete.getId(), owner));
        assertTrue(contactRepository.existsByIdAndUser(toKeep.getId(), owner));
    }

    private User persistUser(String fullName, String email, String phone) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPasswordHash("hashed");
        user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        return userRepository.save(user);
    }

    private Contact saveContact(User user, String firstName, String lastName) {
        Contact contact = new Contact(user, firstName, lastName, null);
        return contactRepository.save(contact);
    }
}
