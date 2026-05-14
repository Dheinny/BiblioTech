package com.fenix.bibliotech.mapper;

import com.fenix.bibliotech.domain.model.Loan;
import com.fenix.bibliotech.dto.response.LoanResponseDTO;
import org.springframework.stereotype.Component;

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


}
