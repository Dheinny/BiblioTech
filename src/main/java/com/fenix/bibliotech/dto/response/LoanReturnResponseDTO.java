package com.fenix.bibliotech.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record LoanReturnResponseDTO(
    UUID id,
    String bookTitle,
    LocalDate loanDate,
    LocalDate dueDate,
    LocalDate returnDate
) {}
