package com.contactmanagement.repository;

import com.contactmanagement.entity.Contact;
import com.contactmanagement.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findByUser(User user, Pageable pageable);

    Optional<Contact> findByIdAndUser(Long id, User user);

    @Query("SELECT c FROM Contact c WHERE c.user = :user AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Contact> searchByFirstNameOrLastName(@Param("user") User user, 
                                               @Param("searchTerm") String searchTerm,
                                               Pageable pageable);

    void deleteByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
