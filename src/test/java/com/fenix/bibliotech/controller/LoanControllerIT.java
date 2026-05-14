package com.fenix.bibliotech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenix.bibliotech.domain.constant.LoanConflictScenario;
import com.fenix.bibliotech.domain.constant.LoanNotFoundScenario;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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
        String customerName = "Customer1";
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
    @MethodSource("com.fenix.bibliotech.controller.LoanControllerIT#invalidLoanRequestProvider")
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

    @ParameterizedTest
    @DisplayName("Deve retornar 404 quando o livro a ser alugado não estiver cadastrado no banco de dados ou estiver com todas licenças inativas")
    @MethodSource("com.fenix.bibliotech.controller.LoanControllerIT#notFoundBookEligibleToLoanProvider")
    public void shouldReturn404WhenBookHasNoEligibleLicensesForLoan(LoanNotFoundScenario scenario) throws Exception {
        // GIVEN
        Map<LoanNotFoundScenario, UUID> NotFoundMap = loanFactory.createBookByNotFoundScenarioMap();

        LoanRequestDTO loanRequest = LoanRequestDTO.builder()
                .bookId(NotFoundMap.get(scenario))
                .customerName("Customer1")
                .build();

        // WHEN & THEN
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message")
                        .value(getMessage(scenario.getExpectedMessageCode(), loanRequest.bookId())));
    }

    @ParameterizedTest
    @DisplayName("Deve retornar 409 quando o livre fizer parte do acerto, porém está com todas licenças ativas emprestadas")
    @MethodSource("com.fenix.bibliotech.controller.LoanControllerIT#bookConflictToLoanProvider")
    public void shouldReturn409WhenBookHasAllLicensesBusy(LoanConflictScenario scenario) throws Exception {
        // GIVEN
        Map<LoanConflictScenario, UUID> conflictMap = loanFactory.createBookByConflictScenarioMap();

        LoanRequestDTO loanRequest = LoanRequestDTO.builder()
                .bookId(conflictMap.get(scenario))
                .customerName("Customer1")
                .build();

        // WHEN & THEN
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.message")
                        .value(getMessage(scenario.getExpectedMessageCode(), loanRequest.bookId())));
    }

    private static Stream<Arguments> invalidLoanRequestProvider() {
        return Stream.of(
                Arguments.of(new LoanRequestDTO(null, "Customer1"),
                        "bookId",
                        "book.id.required"),
                Arguments.of(new LoanRequestDTO(UUID.randomUUID(), ""),
                        "customerName",
                        "customer.identification.required"),
                Arguments.of(new LoanRequestDTO(UUID.randomUUID(), "   "),
                        "customerName",
                        "customer.identification.required")
        );
    }

    private static Stream<Arguments> notFoundBookEligibleToLoanProvider() {
        return Stream.of(
                Arguments.of(LoanNotFoundScenario.BOOK_NOT_FOUND),
                Arguments.of(LoanNotFoundScenario.NO_LICENSES),
                Arguments.of(LoanNotFoundScenario.INACTIVE_LICENSES)
        );
    }

    private static Stream<Arguments> bookConflictToLoanProvider() {
        return Stream.of(
                Arguments.of(LoanConflictScenario.ALL_LICENSES_BUSY),
                Arguments.of(LoanConflictScenario.MIXED_LICENSES)

        );
    }

    private String getMessage(String code) {
        return getMessage(code, null);
    }

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(
                code, args, Locale.of("pt", "BR"));
    }
}