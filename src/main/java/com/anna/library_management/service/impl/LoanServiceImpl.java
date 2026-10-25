package com.anna.library_management.service.impl;

import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Loan;
import com.anna.library_management.entity.LoanStatus;
import com.anna.library_management.entity.User;
import com.anna.library_management.repository.BookRepository;
import com.anna.library_management.repository.LoanRepository;
import com.anna.library_management.repository.UserRepository;
import com.anna.library_management.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private static final int DEFAULT_LOAN_DAYS = 14;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Loan> findAll() {
        return loanRepository.findAll();
    }

    @Override
    public Optional<Loan> findById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public List<Loan> findByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    @Override
    public Loan createLoan(Long bookId, Long userId, Integer days) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Книга не найдена: " + bookId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Пользователь не найден: " + userId));

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Нет доступных копий книги");
        }
        if (loanRepository.findActiveLoanByBookAndUser(bookId, userId).isPresent()) {
            throw new IllegalStateException("Пользователь уже взял эту книгу");
        }

        int loanDays = (days == null) ? DEFAULT_LOAN_DAYS : days;
        LocalDateTime now = LocalDateTime.now();

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan(book, user, now, now.plusDays(loanDays), LoanStatus.ACTIVE);
        return loanRepository.save(loan);
    }

    @Override
    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Выдача не найдена: " + loanId));
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("Книга уже возвращена");
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDateTime.now());

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }
}
