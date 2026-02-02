package com.fenix.bibliotech.dto.response;

import java.util.UUID;

public record BookResponseDTO(
    UUID id,
    String title,
    String author,
    String isbn,
    Integer pages,
    String summary
) {}
