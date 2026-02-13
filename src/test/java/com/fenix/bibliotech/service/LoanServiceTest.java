package com.fenix.bibliotech.service;

import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import com.fenix.bibliotech.domain.policy.LoanPolicy;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.exception.ResourceNotFoundException;
import com.fenix.bibliotech.mapper.LoanMapper;
import com.fenix.bibliotech.repository.BookLicenseRepository;
import com.fenix.bibliotech.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private BookLicenseRepository bookLicenseRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private LoanPolicy loanPolicy;

    @InjectMocks
    private LoanService loanService;

    @Autowired
    private LoanRequestDTO requestDTO;
    private UUID bookId;

    @Mock
    private Clock clock;

    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        requestDTO = new LoanRequestDTO(bookId, "Fulano de Tal");
        fixedInstant = LocalDateTime.of(2026, 2, 13, 10, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o livro não existir no catálogo")
    void shouldThrowExceptionWhenBookNotFound() {
        // GIVEN

        // Quando o count for chamado com qualquer UUID, retorne 0
        when(bookLicenseRepository.countByBookIdAndActiveTrue(any(UUID.class)))
                .thenReturn(0);

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> loanService.checkoutBook(requestDTO));

        verify(bookLicenseRepository, never()).findAvailableLicense(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando não houver licença disponível")
    void shouldThrowExceptionWhenNoLicenseIsAvailable() {
        // GIVEN
        when(bookLicenseRepository.countByBookIdAndActiveTrue(bookId))
                .thenReturn(1);

        when(bookLicenseRepository.findAvailableLicense(bookId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> loanService.checkoutBook(requestDTO));
        verify(bookLicenseRepository, atMostOnce()).findAvailableLicense(bookId);
    }

    @Test
    @DisplayName("Deve realizar o empréstimo com sucesso quando os dados forem válidos")
    void shouldCreateLoanSuccessfuly() {
        // GIVEN
        LocalDate dueDateExpected = LocalDate.ofInstant(fixedInstant, ZoneOffset.UTC)
                .plusDays(14);

        UUID licenseID = UUID.randomUUID();
        String licenseCode = "COD1";
        BookLicense license = BookLicense.builder()
                .id(licenseID)
                .licenseCode(licenseCode)
                .book(Book.builder().id(bookId).build())
                .build();

        when(bookLicenseRepository.countByBookIdAndActiveTrue(bookId))
                .thenReturn(1);
        when(bookLicenseRepository.findAvailableLicense(bookId))
                .thenReturn(Optional.of(license));
        when(loanPolicy.calculateDueDate(any()))
                .thenReturn(dueDateExpected);

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);

        // WHEN
        loanService.checkoutBook(requestDTO);

        // THEN
        verify(loanRepository).save(loanCaptor.capture());
        Loan loanSaved = loanCaptor.getValue();

        assertEquals(LocalDate.ofInstant(fixedInstant, ZoneOffset.UTC), loanSaved.getLoanDate());
        assertEquals(dueDateExpected, loanSaved.getDueDate());
        assertEquals(licenseID, loanSaved.getBookLicense().getId());
        assertEquals(bookId, loanSaved.getBookLicense().getBook().getId());

        UUID customerIdExpected = UUID.nameUUIDFromBytes(
                requestDTO.customerName()
                        .getBytes(StandardCharsets.UTF_8));
        assertEquals(customerIdExpected, loanSaved.getCustomerId());

        verify(loanMapper).toResponse(any(), eq(requestDTO.customerName()));
    }
}