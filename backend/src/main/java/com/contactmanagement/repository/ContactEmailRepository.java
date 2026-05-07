package com.contactmanagement.repository;

import com.contactmanagement.entity.ContactEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactEmailRepository extends JpaRepository<ContactEmail, Long> {
}
