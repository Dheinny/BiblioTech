package com.fenix.bibliotech.service;

import com.fenix.bibliotech.domain.helper.CustomerIdentifier;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import com.fenix.bibliotech.domain.policy.LoanPolicy;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.dto.response.LoanResponseDTO;
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
    public LoanResponseDTO checkoutBook(LoanRequestDTO loanDto) {
        UUID customerID = CustomerIdentifier.generateId(loanDto.customerName());

        boolean hasActiveLicense = bookLicenseRepository.countByBookIdAndActiveTrue(loanDto.bookId()) > 0;
        if (!hasActiveLicense) throw new ResourceNotFoundException("loan.not.eligible.book");

        BookLicense bookLicense = bookLicenseRepository.findAvailableLicense(loanDto.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("loan.not.available"));

        Loan loan = getLoan(customerID, bookLicense);

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
