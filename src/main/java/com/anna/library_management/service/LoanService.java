package com.anna.library_management.service;

import com.anna.library_management.entity.Loan;
import java.util.List;
import java.util.Optional;

public interface LoanService {
    List<Loan> findAll();

    Optional<Loan> findById(Long id);

    List<Loan> findByUserId(Long userId);

    Loan createLoan(Long bookId, Long userId, Integer days);

    Loan returnLoan(Long loanId);
}
