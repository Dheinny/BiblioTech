package com.fenix.bibliotech.factory;

import com.fenix.bibliotech.domain.constant.LoanNotFoundScenario;
import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.repository.BookLicenseRepository;
import com.fenix.bibliotech.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.boot.test.context.TestComponent;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@TestComponent
@RequiredArgsConstructor
public class LoanIntegrationFactory {

    private final BookRepository bookRepository;
    private final BookLicenseRepository bkLicenseRepository;

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
        return bkLicenseRepository.save(bkLicense);
    }

    private BookLicense getBookLicense(Book book, String licenseCode) {
        return getBookLicense(book, licenseCode, true);
    }

    private BookLicense getBookLicense(Book book, String licenseCode, boolean isActive) {
        return BookLicense.builder()
                .licenseCode(licenseCode)
                .book(book)
                .active(isActive)
                .build();
    }

    public Map<LoanNotFoundScenario, UUID> createBookByScenarioMap() {
        Map<LoanNotFoundScenario, UUID> bookByScenarioMap = new EnumMap<>(LoanNotFoundScenario.class);
        bookByScenarioMap.put(LoanNotFoundScenario.BOOK_NOT_FOUND, UUID.randomUUID());

        Book bookNoLicense = bookRepository.save(BookFactory.BookValid1());
        bookByScenarioMap.put(LoanNotFoundScenario.NO_LICENSES, bookNoLicense.getId());

        Book bookInactiveLicense = bookRepository.save(BookFactory.BookValid2());
        BookLicense bookInactiveLicenseLicense1 = getBookLicense(bookInactiveLicense, "BLC2", false);
        bkLicenseRepository.save(bookInactiveLicenseLicense1);
        bookByScenarioMap.put(LoanNotFoundScenario.INACTIVE_LICENSES, bookInactiveLicense.getId());

        return bookByScenarioMap;
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

    private static Stream<Arguments> notFoundBookEligibleToLoanProvider() {
        return Stream.of(
                Arguments.of(LoanNotFoundScenario.BOOK_NOT_FOUND),
                Arguments.of(LoanNotFoundScenario.NO_LICENSES),
                Arguments.of(LoanNotFoundScenario.INACTIVE_LICENSES)
        );
    }
}
