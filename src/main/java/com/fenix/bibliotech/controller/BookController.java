package com.fenix.bibliotech.controller;

import com.fenix.bibliotech.dto.request.BookRequestDTO;
import com.fenix.bibliotech.dto.response.BookResponseDTO;
import com.fenix.bibliotech.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor // Cria o construtor pra injetar o Service automaticamente
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDTO> create(@RequestBody @Valid BookRequestDTO bookDTO) {
        BookResponseDTO savedBook = bookService.save(bookDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBook.id())
                .toUri();

        return ResponseEntity.created(uri).body(savedBook);
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> listAll() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> findBook(@PathVariable UUID id) {
        BookResponseDTO response = bookService.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
