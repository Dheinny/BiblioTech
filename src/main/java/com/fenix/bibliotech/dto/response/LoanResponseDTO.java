package com.fenix.bibliotech.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record LoanResponseDTO(
    UUID id,
    UUID customerId,
    String customerName,
    String bookTitle,
    String licenseCode,
    LocalDate loanDate,
    LocalDate dueDate
){}
