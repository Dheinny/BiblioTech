package com.fenix.bibliotech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.dto.request.BookRequestDTO;
import com.fenix.bibliotech.factory.BookFactory;
import com.fenix.bibliotech.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository repository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldReturn201WhenCreateBookWithSuccess() throws Exception {
        // GIVEN
        BookRequestDTO bookDTO = BookFactory.bookToCreateValid();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isCreated())
                // Valida se o Header Location existe e tem o caminho correto
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/books/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(bookDTO.title()))
                .andExpect(jsonPath("$.author").value(bookDTO.author()));
    }

    @Test
    @DisplayName("Deve retornar 200 e o livro quando o ID existir")
    void shouldReturn200WhenIdExists() throws Exception {
        // GIVEN
        Book book = BookFactory.BookValid1();

        Book savedBook = repository.save(book);

        // WHEN & THEN
        mockMvc.perform(get("/api/books/" + savedBook.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(book.getTitle()))
                .andExpect(jsonPath("$.author").value(book.getAuthor()));
    }

    @Test
    @DisplayName("Deve retornar um erro 404 indicando que o livro procurado não existe")
    public void shouldReturn404WhenBookDoesNotExists() throws Exception {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();

        // WHEN & THEN
        mockMvc.perform(get("/api/books/" + nonExistentId)
                        .header("Accept-Language", "pt-BR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").value(getMessage("book.not.found")))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 204 quando um livro existente é deletado")
    public void shouldReturn204WhenDeleteIsSuccessful() throws Exception {
        // GIVEN
        Book book = BookFactory.BookValid1();

        repository.save(book);

        //WHEN
        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());

        // THEN
        Boolean existsBook = repository.existsById(book.getId());
        assertThat(existsBook).isFalse();
    }

    @Test
    @DisplayName("Deve retornar 404 indicando que o livro a ser deletado não existe no banco")
    public void shouldReturn404WhenDeleteNonExistentBook() throws Exception {
        // GIVEN
        UUID deleteId = UUID.randomUUID();

        // WHEN
        mockMvc.perform(delete("/api/books/{id}", deleteId)
                        .header("Accept-Language", "pt-BR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message")
                        .value(getMessage("book.not.found")))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    public String getMessage(String code) {
        return messageSource.getMessage(
                code, null, new Locale("pt", "BR"));
    }
}
