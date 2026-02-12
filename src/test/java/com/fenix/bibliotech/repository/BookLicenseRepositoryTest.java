package com.fenix.bibliotech.repository;

import com.fenix.bibliotech.domain.Book;
import com.fenix.bibliotech.domain.BookLicense;
import com.fenix.bibliotech.factory.BookLicenseRepositoryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(BookLicenseRepositoryFactory.class)
public class BookLicenseRepositoryTest {

    @Autowired
    private BookLicenseRepository repository;

    @Autowired
    private BookLicenseRepositoryFactory bookFactory; // Helper to save test data

    @Test
    @DisplayName("Deve encontrar uma licença quando o livro nunca foi emprestado")
    void findNeverUsedLicenseAvailable() {
        // Arrange
        Book book = bookFactory.createBookBase();
        BookLicense bookLicense = bookFactory.createLicenseNeverUsed(book);

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Assert
        assertThat(blFound).isPresent();
        assertThat(blFound.get().getId()).isEqualTo(bookLicense.getId());
        assertThat(blFound.get().getBook().getTitle()).isEqualTo(book.getTitle());

        assertThat(blFound).isPresent()
                .get().hasFieldOrPropertyWithValue("id", bookLicense.getId())
                .hasFieldOrPropertyWithValue("active", true)
                .extracting(BookLicense::getBook)
                .hasFieldOrPropertyWithValue("title", book.getTitle())
                .hasFieldOrPropertyWithValue("author", book.getAuthor());
    }

    @Test
    @DisplayName("Não deve ser encontrada nenhuma licença para emprestimo")
    void shouldFindNoLicense() {
        // Arrange
        Book book = bookFactory.createBookBase();

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Assert
        assertThat(blFound).isNotPresent();
    }

    @Test
    @DisplayName("Deve retornar uma licença disponível quando a licensa já foi utilizada antes mas está livre agora")
    void shouldReturnLicenseAvailableWhenLicenseWasAlreadyUsedBeforeButItIsFree() {
        // Arrange
        Book book = bookFactory.createBookBase();
        BookLicense license = bookFactory.createLicenseWithLoanReturned(book);

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Assert
        assertThat(blFound).isPresent().get()
                .hasFieldOrPropertyWithValue("id", license.getId())
                .hasFieldOrPropertyWithValue("active", true)
                .extracting(BookLicense::getBook)
                .hasFieldOrPropertyWithValue("title", book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio quando um livro possui apenas uma licença e ela está ocupada")
    void shouldReturnNoLicenseWhenBookWithOnlyOneLicenseIsLoaned() {
        // Arrange
        Book book = bookFactory.createBookBase();
        bookFactory.createLicenseLoaned(book);

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Arrange
        assertThat(blFound).isNotPresent();
    }

    @Test
    @DisplayName("Deve retornar uma licença disponível quando o " +
            "livro tem duas licenças sendo uma emprestada e outra livre")
    void shoulReturnOneAvailableLicenseWhenABookOneLicenseLoanedAndOneLicenseFree() {
        // Arrange
        Book book = bookFactory.createBookBase();
        bookFactory.createLicenseLoaned(book, "LIC2");
        BookLicense licenseFree = bookFactory.createLicenseWithLoanReturned(book);

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Assert
        assertThat(blFound).isPresent().get()
                .hasFieldOrPropertyWithValue("id", licenseFree.getId())
                .hasFieldOrPropertyWithValue("active", true)
                .extracting(BookLicense::getBook)
                .hasFieldOrPropertyWithValue("id", book.getId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando o livro tem mais de uma licença e nenhuma livre")
    void shouldReturnNoLicenseWhenABookHasMultiplesLicensesButAllLoaned() {
        // Arrange
        Book book = bookFactory.createBookBase();
        bookFactory.createLicenseLoaned(book);
        bookFactory.createLicenseLoaned(book, "LIC2");

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Assert
        assertThat(blFound).isNotPresent();
    }

    @Test
    @DisplayName("Deve retornar apenas a primeira licensa quando livro tiver mais de uma licença disponível")
    void shouldReturnFirstLicenseWhenBookHasManyAvailableLicense() {
        // Arrange
        Book book = bookFactory.createBookBase();
        BookLicense licenseFree1 = bookFactory.createLicenseWithLoanReturned(book);
        bookFactory.createLicenseWithLoanReturned(book, "LIC2");

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Assert
        assertThat(blFound).isPresent().get()
                .hasFieldOrPropertyWithValue("id", licenseFree1.getId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando o livro não tem licensa ativa")
    void shouldReturnNoLicenseWhenThereIsAneActiveLicense() {
        // Arrange
        Book book = bookFactory.createBookBase();
        bookFactory.createLicenseInactive(book);

        // Act
        Optional<BookLicense> blFound = repository.findAvailableLicense(book.getId());

        // Assert
        assertThat(blFound).isNotPresent();
    }
}
