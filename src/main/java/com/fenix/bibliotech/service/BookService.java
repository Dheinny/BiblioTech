package com.fenix.bibliotech.service;

import com.fenix.bibliotech.domain.Book;
import com.fenix.bibliotech.dto.request.BookRequestDTO;
import com.fenix.bibliotech.dto.response.BookResponseDTO;
import com.fenix.bibliotech.exception.BusinessException;
import com.fenix.bibliotech.exception.ResourceNotFoundException;
import com.fenix.bibliotech.mapper.BookMapper;
import com.fenix.bibliotech.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final BookMapper mapper;

    @Transactional
    public BookResponseDTO save(BookRequestDTO bookRequest) {

        if (repository.existsByIsbn(bookRequest.isbn())) {
            throw new BusinessException("book.isbn.already.exists");
        }

        Book book = mapper.toEntity(bookRequest);

        Book savedBook = repository.save(book);
        return mapper.toResponse(book);
    }

    public BookResponseDTO findById(UUID id) {
        return mapper.toResponse(this.findEntityById(id));
    }

    public List<BookResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void deleteById(UUID id){
        repository.delete(this.findEntityById(id));
    }

    private Book findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("book.not.found"));
    }
}
