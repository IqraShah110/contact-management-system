package com.contactmanagement.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.contactmanagement.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindAndCheckExistenceByEmail() {
        User user = new User();
        user.setFullName("Ali Khan");
        user.setEmail("ali@example.com");
        user.setPhoneNumber("03001234567");
        user.setPasswordHash("hashedPassword");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("ali@example.com"));
        assertTrue(userRepository.findByEmail("ali@example.com").isPresent());
        assertFalse(userRepository.findByEmail("unknown@example.com").isPresent());
    }

    @Test
    void shouldFindAndCheckExistenceByPhoneNumber() {
        User user = new User();
        user.setFullName("Sara Khan");
        user.setEmail("sara@example.com");
        user.setPhoneNumber("03111234567");
        user.setPasswordHash("hashedPassword");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        assertTrue(userRepository.existsByPhoneNumber("03111234567"));
        assertTrue(userRepository.findByPhoneNumber("03111234567").isPresent());
        assertFalse(userRepository.findByPhoneNumber("00000000000").isPresent());
    }
}
