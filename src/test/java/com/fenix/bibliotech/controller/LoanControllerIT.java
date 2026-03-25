package com.fenix.bibliotech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenix.bibliotech.domain.helper.CustomerIdentifier;
import com.fenix.bibliotech.domain.model.Book;
import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.factory.LoanIntegrationFactory;
import com.fenix.bibliotech.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(LoanIntegrationFactory.class)
public class LoanControllerIT {
    private static final String BASE_URL = "/api/v1/loans";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanIntegrationFactory loanFactory;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturn201WhenCreateLoanWithSuccess() throws Exception {
        // GIVEN
        Book book = loanFactory.createBookBase();
        BookLicense bkLicense = loanFactory.createBookLicense(book);
        String customerName = "Nome1";
        LoanRequestDTO loanRequest = LoanRequestDTO.builder()
                .bookId(book.getId())
                .customerName(customerName)
                .build();
        UUID customerIdExpected = CustomerIdentifier.generateId(customerName);

        // WHEN & THEN
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/v1/loans/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.customerId").value(customerIdExpected.toString()))
                .andExpect(jsonPath("$.customerName").value(customerName))
                .andExpect(jsonPath("$.bookTitle").value(book.getTitle()))
                .andExpect(jsonPath("$.licenseCode").value(bkLicense.getLicenseCode()))
                .andExpect(jsonPath("$.loanDate").isNotEmpty())
                .andExpect(jsonPath("$.dueDate").isNotEmpty());

        List<Loan> loan = loanRepository.findAll();

        assertThat(loan).hasSize(1).first()
                .satisfies(l -> {
                    assertThat(l.getBookLicense())
                            .hasFieldOrPropertyWithValue("licenseCode", bkLicense.getLicenseCode())
                            .extracting(BookLicense::getBook)
                            .hasFieldOrPropertyWithValue("title", book.getTitle());
                    assertThat(l.getDueDate()).isAfter(l.getLoanDate());
                    assertThat(l.getReturnDate()).isNull();
                });
    }


    @ParameterizedTest
    @DisplayName("Deve retornar 400 indicando que os campos da solicitação de empréstimo estão inválidos.")
    @MethodSource("com.fenix.bibliotech.factory.LoanIntegrationFactory#invalidLoanRequestProvider")
    public void shouldReturn400WhenLoanFieldsAreInvalid(LoanRequestDTO loanRequest, String expectedField, String errorMessageCode) throws Exception {
        // WHEN
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + expectedField).value(getMessage(errorMessageCode)));

        // THEN
        assertThat(loanRepository.findAll().isEmpty());
    }

    private String getMessage(String code) {
        return messageSource.getMessage(
                code, null, new Locale("pt", "BR"));
    }
}