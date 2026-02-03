package com.fenix.bibliotech.mapper;

import com.fenix.bibliotech.domain.Book;
import com.fenix.bibliotech.dto.request.BookRequestDTO;
import com.fenix.bibliotech.dto.response.BookResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookResponseDTO toResponse(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPages(),
                book.getSummary()
        );
    }

    public Book toEntity(BookRequestDTO request) {
        return Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .pages(request.pages())
                .summary(request.summary())
                .build();
    }
}
