package com.contactmanagement.repository;

import com.contactmanagement.entity.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactPhoneRepository extends JpaRepository<ContactPhone, Long> {
}
