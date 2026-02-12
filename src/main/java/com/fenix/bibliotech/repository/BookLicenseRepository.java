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

    @Query("SELECT bl FROM BookLicense bl WHERE bl.book.id = :bookId AND bl.active = TRUE " +
            "AND NOT " +
            "EXISTS (SELECT l FROM Loan l WHERE l.bookLicense = bl " +
            "AND l.returnDate IS NULL)")
    List<BookLicense> findAvailableLicense(UUID bookId, Pageable pageable);

    default Optional<BookLicense> findAvailableLicense(UUID bookId) {
        return findAvailableLicense(bookId, PageRequest.of(0, 1))
                .stream().findFirst();
    }
}
