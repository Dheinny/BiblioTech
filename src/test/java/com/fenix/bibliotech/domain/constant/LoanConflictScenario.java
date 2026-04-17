package com.fenix.bibliotech.domain.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LoanConflictScenario {
    ALL_LICENSES_BUSY("loan.not.available"),
    MIXED_LICENSES("loan.not.available");

    private final String expectedMessageCode;
}
