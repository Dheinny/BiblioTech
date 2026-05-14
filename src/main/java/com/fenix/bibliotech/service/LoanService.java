package com.fenix.bibliotech.service;

import com.fenix.bibliotech.domain.helper.CustomerIdentifier;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import com.fenix.bibliotech.domain.policy.LoanPolicy;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.dto.response.LoanResponseDTO;
import com.fenix.bibliotech.exception.ResourceConflictException;
import com.fenix.bibliotech.exception.ResourceNotFoundException;
import com.fenix.bibliotech.mapper.LoanMapper;
import com.fenix.bibliotech.repository.BookLicenseRepository;
import com.fenix.bibliotech.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
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
    public LoanResponseDTO checkoutBook(LoanRequestDTO loanDTO) {
        UUID customerID = CustomerIdentifier.generateId(loanDTO.customerName());

        boolean hasActiveLicense = bookLicenseRepository.countByBookIdAndActiveTrue(loanDTO.bookId()) > 0;
        if (!hasActiveLicense) throw new ResourceNotFoundException("loan.not.eligible.book", loanDTO.bookId());

        BookLicense bookLicense = bookLicenseRepository.findAvailableLicense(loanDTO.bookId())
                .orElseThrow(() -> new ResourceConflictException("loan.not.available", loanDTO.bookId()));

        Loan loan = getLoan(customerID, bookLicense);

        return loanMapper.toResponse(
                loanRepository.save(loan),
                loanDTO.customerName()
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
