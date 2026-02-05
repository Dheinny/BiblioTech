package com.fenix.bibliotech.repository;

import com.fenix.bibliotech.domain.Book;
import com.fenix.bibliotech.domain.BookLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookLicenseRepository extends JpaRepository<BookLicense, UUID> {
    // Return total license for a book
    long countByBook(Book book);

    // Return total of active licenses for a book
    long countByBookAndActiveTrue(Book book);
}
