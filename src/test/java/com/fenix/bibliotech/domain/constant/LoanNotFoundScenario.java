package com.fenix.bibliotech.domain.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LoanNotFoundScenario {
    BOOK_NOT_FOUND("loan.not.eligible.book"),
    INACTIVE_LICENSES("loan.not.eligible.book"),
    NO_LICENSES("loan.not.eligible.book");

    private final String expectedMessageCode;
}
