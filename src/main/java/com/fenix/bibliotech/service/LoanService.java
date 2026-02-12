package com.fenix.bibliotech.service;

import com.fenix.bibliotech.domain.BookLicense;
import com.fenix.bibliotech.domain.Loan;
import com.fenix.bibliotech.domain.LoanPolicy;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.dto.response.LoanResponseDTO;
import com.fenix.bibliotech.mapper.LoanMapper;
import com.fenix.bibliotech.repository.BookLicenseRepository;
import com.fenix.bibliotech.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final BookLicenseRepository bookLicenseRepository;
    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;
    private final LoanPolicy loanPolicy;
    private final Clock clock;


    @Transactional
    public LoanResponseDTO checkoutBook(LoanRequestDTO loanDto) {
        UUID customerID = UUID.nameUUIDFromBytes(loanDto.customerName().getBytes(StandardCharsets.UTF_8));

        Optional<BookLicense> bookLicense = bookLicenseRepository.findAvailableLicense(loanDto.bookId());
        bookLicense.orElseThrow();

        Loan loan = getLoan(customerID, bookLicense.get());

        return loanMapper.toResponse(
                loanRepository.save(loan),
                loanDto.customerName()
        );
    }

    private Loan getLoan(UUID customerID, BookLicense bookLicense) {
        LocalDate today = LocalDate.now(clock);
        LocalDate dueDate = loanPolicy.calculateDueDate(today);
        return Loan.builder()
                .customerId(customerID)
                .loanDate(today)
                .dueDate(dueDate)
                .bookLicense(bookLicense)
                .build();

    }
}
