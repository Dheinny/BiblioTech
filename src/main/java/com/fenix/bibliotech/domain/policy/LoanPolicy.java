package com.fenix.bibliotech.domain.policy;

import com.fenix.bibliotech.config.LoanProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LoanPolicy {
    private final LoanProperties loanProp;

    public LocalDate calculateDueDate(LocalDate baseDate) {
        return baseDate.plusDays(loanProp.getDefaultTermDays());
    }
}
