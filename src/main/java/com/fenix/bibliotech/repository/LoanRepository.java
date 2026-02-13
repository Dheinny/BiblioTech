package com.fenix.bibliotech.repository;

import com.fenix.bibliotech.domain.model.BookLicense;
import com.fenix.bibliotech.domain.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByBookLicenseAndReturnDateIsNull (BookLicense bookLicense);
}
