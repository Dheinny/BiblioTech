package com.fenix.bibliotech.repository;

import com.fenix.bibliotech.domain.Book;
import com.fenix.bibliotech.domain.BookLicense;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookLicenseRepository extends JpaRepository<BookLicense, UUID> {
    // Return total license for a book
    long countByBook(Book book);

    // Return total of active licenses for a book
    long countByBookAndActiveTrue(Book book);

    @Query("SELECT bl FROM BookLicense bl WHERE bl.book = :book AND bl.active = TRUE " +
            "AND NOT " +
            "EXISTS (SELECT l FROM Loan l WHERE l.bookLicense = bl " +
            "AND l.returnDate IS NULL)")
    List<BookLicense> findAvailableLicense(Book book, Pageable pageable);

    default Optional<BookLicense> findAvailableLicense(Book book) {
        return findAvailableLicense(book, PageRequest.of(0,1))
                .stream().findFirst();
    }
}
