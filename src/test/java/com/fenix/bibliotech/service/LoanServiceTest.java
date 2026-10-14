package com.fenix.bibliotech.service;

import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import com.fenix.bibliotech.domain.policy.LoanPolicy;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.dto.response.LoanResponseDTO;
import com.fenix.bibliotech.dto.response.LoanReturnResponseDTO;
import com.fenix.bibliotech.exception.AccessDeniedException;
import com.fenix.bibliotech.exception.ResourceConflictException;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private BookLicenseRepository bookLicenseRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanPolicy loanPolicy;

    @Spy
    LoanMapper loanMapper = new LoanMapper();

    @InjectMocks
    private LoanService loanService;

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
        lenient().when(clock.instant()).thenReturn(fixedInstant);
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o livro não existir no catálogo")
    void shouldThrowExceptionWhenBookNotFound() {
        // GIVEN

        // Quando o count for chamado com qualquer UUID, retorne 0
        when(bookLicenseRepository.countByBookIdAndActiveTrue(any(UUID.class)))
                .thenReturn(0);

        // WHEN & THEN
        assertThatThrownBy(() -> loanService.checkoutBook(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("loan.not.eligible.book");

        verify(bookLicenseRepository, never()).findAvailableLicense(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceConflictException quando não houver licença disponível")
    void shouldThrowExceptionWhenNoLicenseIsAvailable() {
        // GIVEN
        when(bookLicenseRepository.countByBookIdAndActiveTrue(bookId))
                .thenReturn(1);

        when(bookLicenseRepository.findAvailableLicense(bookId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> loanService.checkoutBook(requestDTO))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("loan.not.available");

        verify(bookLicenseRepository, atMostOnce()).findAvailableLicense(bookId);
    }

    @Test
    @DisplayName("Deve realizar o empréstimo com sucesso quando os dados forem válidos")
    void shouldCreateLoanSuccessfully() {
        // GIVEN
        LocalDate loanDateExpected = LocalDate.ofInstant(fixedInstant, ZoneOffset.UTC);
        LocalDate dueDateExpected = loanDateExpected.plusDays(14);

        UUID licenseId = UUID.randomUUID();
        String licenseCode = "COD1";
        BookLicense license = BookLicense.builder()
                .id(licenseId)
                .licenseCode(licenseCode)
                .book(Book.builder().id(bookId).title("Livro1").build())
                .build();

        UUID customerIdExpected = UUID.nameUUIDFromBytes(
                requestDTO.customerName()
                        .getBytes(StandardCharsets.UTF_8));

        LoanResponseDTO responseExpected = LoanResponseDTO.builder()
                .customerId(customerIdExpected)
                .customerName(requestDTO.customerName())
                .bookTitle("Livro1")
                .licenseCode("COD1")
                .loanDate(loanDateExpected)
                .dueDate(dueDateExpected)
                .build();

        when(bookLicenseRepository.countByBookIdAndActiveTrue(bookId))
                .thenReturn(1);
        when(bookLicenseRepository.findAvailableLicense(bookId))
                .thenReturn(Optional.of(license));
        when(loanPolicy.calculateDueDate(any()))
                .thenReturn(dueDateExpected);
        when(loanRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);

        // WHEN
        LoanResponseDTO loanResponse = loanService.checkoutBook(requestDTO);

        // THEN
        verify(loanRepository).save(loanCaptor.capture());
        Loan loanSaved = loanCaptor.getValue();

        assertThat(loanSaved)
                .returns(loanDateExpected, Loan::getLoanDate)
                .returns(dueDateExpected, Loan::getDueDate)
                .returns(customerIdExpected, Loan::getCustomerId)
                .extracting(Loan::getBookLicense)
                .returns(licenseId, BookLicense::getId)
                .returns(bookId, bkLicense -> bkLicense.getBook().getId());

        assertThat(loanResponse)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(responseExpected);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando tentar devolver um empréstimo que não existe")
    void shouldThrowExceptionWhenReturningNonExistentLoan() {
        // GIVEN
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> loanService.returnBook(loanId, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("loan.returning.non.existent");

        verify(loanRepository, atMostOnce()).findById(loanId);
    }

    @Test
    @DisplayName("Deve lançar ResourceConflictException quando tentar devolver um empréstimo que já foi devolvido")
    void shouldThrowExceptionWhenLoanWasAlreadyReturned() {
        // GIVEN
        UUID loanId = UUID.randomUUID();
        Loan loanReturned = Loan.builder()
                .id(loanId)
                .returnDate(LocalDate.ofInstant(fixedInstant, ZoneOffset.UTC))
                .build();

        when(loanRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(loanReturned));

        // WHEN & THEN
        assertThatThrownBy(() -> loanService.returnBook(loanId, UUID.randomUUID()))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("loan.already.returned");
    }

    @Test
    @DisplayName("Deve realizar a devolução do empréstimo e retornar sucesso")
    void shouldReturnBookSuccessfully() {
        // GIVEN
        UUID loanId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        LocalDate returnDate = LocalDate.ofInstant(fixedInstant, ZoneOffset.UTC);
        LocalDate loanDate = returnDate.minusDays(13);
        LocalDate dueDateExpected = loanDate.plusDays(14);

        Loan loan = Loan.builder()
                .id(loanId)
                .customerId(customerId)
                .loanDate(loanDate)
                .dueDate(dueDateExpected)
                .bookLicense(BookLicense.builder().id(UUID.randomUUID())
                        .licenseCode("COD1")
                        .book(Book.builder().id(UUID.randomUUID())
                                .title("Book of Test").build())
                        .build())
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);

        // WHEN
        LoanReturnResponseDTO returnResponse = loanService.returnBook(loanId, customerId);

        // THEN
        verify(loanRepository).save(loanCaptor.capture());
        Loan loanReturned = loanCaptor.getValue();

        assertThat(loanReturned)
                .returns(loanId, Loan::getId)
                .returns(customerId, Loan::getCustomerId)
                .returns(loanDate, Loan::getLoanDate)
                .returns(dueDateExpected, Loan::getDueDate)
                .returns(returnDate, Loan::getReturnDate)
                .returns(loan.getBookLicense(), Loan::getBookLicense);

        assertThat(returnResponse)
                .returns(loanId, LoanReturnResponseDTO::id)
                .returns(loanDate, LoanReturnResponseDTO::loanDate)
                .returns(dueDateExpected, LoanReturnResponseDTO::dueDate)
                .returns(returnDate, LoanReturnResponseDTO::returnDate)
                .returns("Book of Test", LoanReturnResponseDTO::bookTitle);
    }

    @Test
    @DisplayName("Deve retornar AccessDeniedException quando o cliente tenta devolver um emprestimo que não percence a ele")
    void shouldThrowExceptionWhenCustomerIsNotTheLoanOwner() {
        // WHEN
        UUID loanId = UUID.randomUUID();
        UUID customerIdOwner = UUID.randomUUID();
        UUID customerIdAttacker = UUID.randomUUID();

        Loan loan = Loan.builder()
                .id(loanId)
                .customerId(customerIdOwner)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        // WHEN & THEN
        assertThatThrownBy(() -> loanService.returnBook(loanId, customerIdAttacker))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("loan.return.not-owner");

    }
}