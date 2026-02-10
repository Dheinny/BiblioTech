package com.fenix.bibliotech.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

@Builder
public record LoanDTO(
        UUID bookId,
        @NotBlank(message = "{customer.identification.required}")
        String customerName
) {}
