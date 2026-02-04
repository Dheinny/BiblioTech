package com.fenix.bibliotech.controller;

import com.fenix.bibliotech.domain.Book;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 200 e o livro quando o ID existir")
    void shouldReturn200WhenIdExists() throws Exception {
        // GIVEN
        Book book = Book.builder()
                .title("Java Efetivo")
                .author("Joshua Bloch")
                .isbn("987-8576082675").build();

        Book savedBook = repository.save(book);

        // WHEN & THEN
        mockMvc.perform(get("/api/books/" + savedBook.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Efetivo"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    @Test
    @DisplayName("Deve retornar um erro 404 indicando que o livro procurado não existe")
    public void shouldReturn404WhenBookDoesNotExists() throws Exception {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();

        // WHEN & THEN
        String expectedMessage = messageSource.getMessage(
                "book.not.found", null, new Locale("pt", "BR"));
        mockMvc.perform(get("/api/books/" + nonExistentId)
                        .header("Accept-Language", "pt-BR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
