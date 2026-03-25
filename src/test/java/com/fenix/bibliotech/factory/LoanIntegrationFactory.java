package com.fenix.bibliotech.factory;

import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.repository.BookLicenseRepository;
import com.fenix.bibliotech.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.boot.test.context.TestComponent;

import java.util.UUID;
import java.util.stream.Stream;

@TestComponent
@RequiredArgsConstructor
public class LoanIntegrationFactory {

    private final BookRepository bookRepository;
    private final BookLicenseRepository bkLicenseRepo;

    public Book createBookBase() {
        Book book = Book.builder()
                .title("Book1")
                .author("Author1")
                .isbn("123456")
                .pages(145)
                .build();

        return bookRepository.save(book);
    }

    public BookLicense createBookLicense(Book book) {
        BookLicense bkLicense = getBookLicense(book, "COD1");
        return bkLicenseRepo.save(bkLicense);
    }

    private BookLicense getBookLicense(Book book, String licenseCode) {
        return BookLicense.builder()
                .licenseCode(licenseCode)
                .book(book)
                .build();
    }

    private static Stream<Arguments> invalidLoanRequestProvider() {
        return Stream.of(
                Arguments.of(new LoanRequestDTO(null, "Customer1"),
                        "bookId",
                        "book.id.required"),
                Arguments.of(new LoanRequestDTO(UUID.randomUUID(), ""),
                        "customerName",
                        "customer.identification.required"),
                Arguments.of(new LoanRequestDTO(UUID.randomUUID(), "   "),
                        "customerName",
                        "customer.identification.required")
        );
    }
}
