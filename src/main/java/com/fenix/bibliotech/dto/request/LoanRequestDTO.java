package com.fenix.bibliotech.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record LoanRequestDTO(
        @NotNull(message = "{book.id.required}")
        UUID bookId,
        @NotBlank(message = "{customer.identification.required}")
        String customerName
) {}
