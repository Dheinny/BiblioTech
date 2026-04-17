package com.fenix.bibliotech.factory;

import com.fenix.bibliotech.domain.constant.LoanConflictScenario;
import com.fenix.bibliotech.domain.constant.LoanNotFoundScenario;
import com.fenix.bibliotech.domain.helper.CustomerIdentifier;
import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import com.fenix.bibliotech.domain.policy.LoanPolicy;
import com.fenix.bibliotech.repository.BookLicenseRepository;
import com.fenix.bibliotech.repository.BookRepository;
import com.fenix.bibliotech.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@TestComponent
@RequiredArgsConstructor
public class LoanIntegrationFactory {

    private final BookRepository bookRepository;
    private final BookLicenseRepository bkLicenseRepository;
    private final LoanRepository loanRepository;

    private final LoanPolicy loanPolicy;


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
        BookLicense bkLicense = BookFactory.getBookLicense(book, "COD1");
        return bkLicenseRepository.save(bkLicense);
    }

    public Map<LoanNotFoundScenario, UUID> createBookByNotFoundScenarioMap() {
        Map<LoanNotFoundScenario, UUID> bookByNotFoundScenarioMap =
                new EnumMap<>(LoanNotFoundScenario.class);
        bookByNotFoundScenarioMap.put(LoanNotFoundScenario.BOOK_NOT_FOUND, UUID.randomUUID());

        Book bookNoLicense = bookRepository.save(BookFactory.BookValid1());
        bookByNotFoundScenarioMap.put(LoanNotFoundScenario.NO_LICENSES, bookNoLicense.getId());

        Book bookInactiveLicense = bookRepository.save(BookFactory.BookValid2());
        BookLicense bookInactiveLicenseLicense1 = BookFactory.getBookLicense(
                bookInactiveLicense, "BLC2", false);
        bkLicenseRepository.save(bookInactiveLicenseLicense1);
        bookByNotFoundScenarioMap.put(LoanNotFoundScenario.INACTIVE_LICENSES, bookInactiveLicense.getId());

        return bookByNotFoundScenarioMap;
    }

    public Map<LoanConflictScenario, UUID> createBookByConflictScenarioMap() {

        Map<LoanConflictScenario, UUID> bookByConflictScenarioMap =
                new EnumMap<>(LoanConflictScenario.class);

        // Scenario: ALL_LICENSES_BUSY
        Book bookAllBusy = bookRepository.save(BookFactory.BookValid1());
        BookLicense bookLicenseAllBusy = bkLicenseRepository.save(BookFactory.getBookLicense(
                bookAllBusy, "BLC_ACT1"));

        loanRepository.save(Loan.builder()
                .customerId(CustomerIdentifier.generateId("Customer1"))
                .loanDate(LocalDate.now())
                .dueDate(loanPolicy.calculateDueDate(LocalDate.now()))
                .bookLicense(bookLicenseAllBusy).build());

        bookByConflictScenarioMap.put(LoanConflictScenario.ALL_LICENSES_BUSY, bookAllBusy.getId());

        // Scenario: MIXED_LICENSES
        Book bookMixedState = bookRepository.save(BookFactory.BookValid2());
        BookLicense bookLicenseBusy = bkLicenseRepository.save(BookFactory.getBookLicense(
                bookMixedState, "BLC_BSY"
        ));

        BookLicense bookLicenseInactive = bkLicenseRepository.save(BookFactory.getBookLicense(
                bookMixedState, "BLC_INA", false
        ));

        loanRepository.save(Loan.builder()
                .customerId(CustomerIdentifier.generateId("Customer1"))
                .loanDate(LocalDate.now())
                .dueDate(loanPolicy.calculateDueDate(LocalDate.now()))
                .bookLicense(bookLicenseInactive).build());

        bookByConflictScenarioMap.put(LoanConflictScenario.MIXED_LICENSES, bookAllBusy.getId());
        return bookByConflictScenarioMap;
    }

}
