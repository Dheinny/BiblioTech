package com.fenix.bibliotech.repository;

import com.fenix.bibliotech.domain.BookLicense;
import com.fenix.bibliotech.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByBookLicenseAndReturnDateIsNull (BookLicense bookLicense);
}
