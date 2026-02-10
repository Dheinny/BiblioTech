package com.fenix.bibliotech.mapper;

import com.fenix.bibliotech.domain.BookLicense;
import com.fenix.bibliotech.domain.Loan;
import com.fenix.bibliotech.dto.response.LoanResponseDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoanMapper {
    public LoanResponseDTO toResponse(Loan loan, String customerName) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .customerId(loan.getCustomerId())
                .customerName(customerName)
                .bookTitle(loan.getBookLicense().getBook().getTitle())
                .licenseCode(loan.getBookLicense().getLicenseCode())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .build();
    }

    public Loan toEntity(BookLicense license, UUID customerId) {
        return Loan.builder()
                .customerId(customerId)
                .bookLicense(license)
                .build();
    }
}
