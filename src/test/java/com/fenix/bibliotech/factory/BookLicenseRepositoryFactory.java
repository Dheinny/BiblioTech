package com.fenix.bibliotech.factory;

import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDate;
import java.util.UUID;

@TestComponent
public class BookLicenseRepositoryFactory {
    private final TestEntityManager entityManager;

    public BookLicenseRepositoryFactory(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Book createBookBase() {
        Book book = Book.builder()
                .title("Book1")
                .author("Author1")
                .isbn("12345")
                .build();

        return entityManager.persist(book);
    }

    public BookLicense createLicenseNeverUsed(Book book) {
        BookLicense license = getBookLicense1(book, "LIC1");

        entityManager.persist(license);

        return license;
    }

    public BookLicense createLicenseWithLoanReturned(Book book) {
        return createLicenseWithLoanReturned(book, "LIC1");
    }

    public BookLicense createLicenseWithLoanReturned(Book book, String licenseCode) {
        // Arrange
        BookLicense license = getBookLicense1(book, licenseCode);
        entityManager.persist(license);

        Loan loan = Loan.builder()
                .customerId(UUID.randomUUID())
                .loanDate(LocalDate.now().minusDays(14))
                .dueDate(LocalDate.now().minusDays(7))
                .returnDate(LocalDate.now().minusDays(7))
                .bookLicense(license)
                .build();

        entityManager.persist(loan);

        return license;
    }


    public BookLicense createLicenseLoaned(Book book) {
        return createLicenseLoaned(book, "LIC1");
    }

    public BookLicense createLicenseLoaned(Book book, String licenseCode) {
        BookLicense license = getBookLicense1(book, licenseCode);

        entityManager.persist(license);

        Loan loan = Loan.builder()
                .customerId(UUID.randomUUID())
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .bookLicense(license)
                .build();

        entityManager.persist(loan);

        return license;
    }

    public BookLicense createLicenseInactive(Book book){
        return entityManager.persist(BookLicense.builder()
                .book(book)
                .licenseCode("LIC1")
                .active(false)
                .build());
    }

    private BookLicense getBookLicense1(Book book, String licenseCode) {
        return BookLicense.builder()
                .licenseCode(licenseCode)
                .book(book)
                .build();
    }
}
