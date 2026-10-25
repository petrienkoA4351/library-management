package com.anna.library_management.controller;

import com.anna.library_management.dto.CreateLoanDTO;
import com.anna.library_management.dto.LoanDTO;
import com.anna.library_management.entity.Loan;
import com.anna.library_management.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    // Получить все выдачи
    @GetMapping
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        List<LoanDTO> loans = loanService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loans);
    }

    // Получить выдачу по ID
    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable Long id) {
        Optional<Loan> loan = loanService.findById(id);
        if (loan.isPresent()) {
            return ResponseEntity.ok(convertToDTO(loan.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Получить выдачи пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanDTO>> getLoansByUser(@PathVariable Long userId) {
        List<LoanDTO> loans = loanService.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loans);
    }

    // Выдать книгу пользователю
    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@Valid @RequestBody CreateLoanDTO dto) {
        Loan loan = loanService.createLoan(dto.getBookId(), dto.getUserId(), dto.getDays());
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(loan));
    }

    // Принять книгу обратно
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanDTO> returnLoan(@PathVariable Long id) {
        Loan loan = loanService.returnLoan(id);
        return ResponseEntity.ok(convertToDTO(loan));
    }

    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setBookId(loan.getBook().getId());
        dto.setBookTitle(loan.getBook().getTitle());
        dto.setUserId(loan.getUser().getId());
        dto.setUserName(loan.getUser().getFirstName() + " " + loan.getUser().getLastName());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnDate(loan.getReturnDate());
        dto.setStatus(loan.getStatus() == null ? null : loan.getStatus().name());
        return dto;
    }
}
