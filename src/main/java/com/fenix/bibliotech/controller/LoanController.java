package com.fenix.bibliotech.controller;

import com.fenix.bibliotech.dto.request.LoanRequestDTO;
import com.fenix.bibliotech.dto.response.LoanResponseDTO;
import com.fenix.bibliotech.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponseDTO> checkout (@Valid @RequestBody LoanRequestDTO loanRequest) {
        LoanResponseDTO loanResponse = loanService.checkoutBook(loanRequest);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(loanResponse.id())
                .toUri();

        return ResponseEntity.created(uri).body(loanResponse);
    }
}

