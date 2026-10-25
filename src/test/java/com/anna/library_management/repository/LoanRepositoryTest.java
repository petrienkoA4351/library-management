package com.anna.library_management.repository;

import com.anna.library_management.TestData;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Loan;
import com.anna.library_management.entity.LoanStatus;
import com.anna.library_management.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        book = bookRepository.save(TestData.book("Book", "111-111", 3));
        user = userRepository.save(TestData.user("reader@library.com"));
    }

    private Loan saveLoan(int loanDaysAgo, int dueInDays, LoanStatus status) {
        return loanRepository.save(new Loan(book, user,
                LocalDateTime.now().minusDays(loanDaysAgo),
                LocalDateTime.now().plusDays(dueInDays), status));
    }

    @Test
    void findByUserId_ShouldReturnLoansOfUser() {
        saveLoan(1, 13, LoanStatus.ACTIVE);

        assertEquals(1, loanRepository.findByUserId(user.getId()).size());
        assertTrue(loanRepository.findByUserId(-1L).isEmpty());
    }

    @Test
    void findByBookId_ShouldReturnLoansOfBook() {
        saveLoan(1, 13, LoanStatus.ACTIVE);

        assertEquals(1, loanRepository.findByBookId(book.getId()).size());
    }

    @Test
    void findByUserIdAndStatus_ShouldFilterByStatus() {
        saveLoan(1, 13, LoanStatus.ACTIVE);
        saveLoan(30, -16, LoanStatus.RETURNED);

        List<Loan> active = loanRepository.findByUserIdAndStatus(user.getId(), LoanStatus.ACTIVE);

        assertEquals(1, active.size());
        assertEquals(LoanStatus.ACTIVE, active.get(0).getStatus());
    }

    @Test
    void findByStatus_ShouldReturnLoansWithStatus() {
        saveLoan(1, 13, LoanStatus.ACTIVE);
        saveLoan(30, -16, LoanStatus.RETURNED);

        assertEquals(1, loanRepository.findByStatus(LoanStatus.RETURNED).size());
    }

    @Test
    void findOverdueLoans_ShouldReturnOnlyExpiredActiveLoans() {
        saveLoan(30, -10, LoanStatus.ACTIVE);
        saveLoan(1, 13, LoanStatus.ACTIVE);
        saveLoan(30, -10, LoanStatus.RETURNED);

        List<Loan> overdue = loanRepository.findOverdueLoans(LocalDateTime.now());

        assertEquals(1, overdue.size());
    }

    @Test
    void findActiveLoanByBookAndUser_ShouldReturnActiveLoan() {
        saveLoan(1, 13, LoanStatus.ACTIVE);

        Optional<Loan> found = loanRepository.findActiveLoanByBookAndUser(book.getId(), user.getId());
        Optional<Loan> notFound = loanRepository.findActiveLoanByBookAndUser(book.getId(), -1L);

        assertTrue(found.isPresent());
        assertTrue(notFound.isEmpty());
    }
}
