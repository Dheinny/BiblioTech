package com.fenix.bibliotech.service;

import com.fenix.bibliotech.domain.Book;
import com.fenix.bibliotech.dto.request.BookRequestDTO;
import com.fenix.bibliotech.dto.response.BookResponseDTO;
import com.fenix.bibliotech.exception.BusinessException;
import com.fenix.bibliotech.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    @Transactional
    public BookResponseDTO save(BookRequestDTO bookDTO) {

        if (repository.existsByIsbn(bookDTO.isbn())) {
            throw new BusinessException("book.isbn.already.exists");
        }

        Book book = Book.builder()
                .title(bookDTO.title())
                .author(bookDTO.author())
                .isbn(bookDTO.isbn())
                .pages(bookDTO.pages())
                .summary(bookDTO.summary())
                .build();

        Book savedBook = repository.save(book);
        return new BookResponseDTO(
                savedBook.getId(),
                savedBook.getTitle(),
                savedBook.getAuthor(),
                savedBook.getIsbn(),
                savedBook.getPages(),
                savedBook.getSummary()
        );
    }

    public List<BookResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(book -> new BookResponseDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getPages(),
                        book.getSummary()
                    )
                ).toList();
    }
}
