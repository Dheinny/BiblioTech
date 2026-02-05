package com.fenix.bibliotech.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record BookRequestDTO (
    @NotBlank(message = "{book.title.required}")
    String title,

    @NotBlank(message = "{book.author.required")
    String author,

    @NotBlank(message = "{book.isbn.required}")
    String isbn,

    @NotNull(message = "{book.pages.required}")
    @Positive
    Integer pages,

    String summary
) {}
