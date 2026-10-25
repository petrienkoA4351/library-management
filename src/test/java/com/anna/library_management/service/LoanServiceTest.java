package com.anna.library_management.service;

import com.anna.library_management.TestData;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Loan;
import com.anna.library_management.entity.LoanStatus;
import com.anna.library_management.entity.User;
import com.anna.library_management.repository.BookRepository;
import com.anna.library_management.repository.LoanRepository;
import com.anna.library_management.repository.UserRepository;
import com.anna.library_management.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        book = TestData.book("Test book", "111-111", 2);
        book.setId(1L);
        user = TestData.user("reader@library.com");
        user.setId(2L);
    }

    @Test
    void createLoan_ShouldDecreaseCopiesAndCreateActiveLoan() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(loanRepository.findActiveLoanByBookAndUser(1L, 2L)).thenReturn(Optional.empty());
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.createLoan(1L, 2L, 7);

        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertEquals(1, book.getAvailableCopies());
        assertTrue(loan.getDueDate().isAfter(loan.getLoanDate()));
        verify(bookRepository).save(book);
    }

    @Test
    void createLoan_WhenBookNotFound_ShouldThrowIllegalArgument() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(99L, 2L, 7));

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void createLoan_WhenNoCopies_ShouldThrowIllegalState() {
        book.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> loanService.createLoan(1L, 2L, 7));

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnLoan_ShouldMarkReturnedAndIncreaseCopies() {
        Loan loan = new Loan(book, user, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(9), LoanStatus.ACTIVE);
        loan.setId(5L);
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.returnLoan(5L);

        assertEquals(LoanStatus.RETURNED, result.getStatus());
        assertNotNull(result.getReturnDate());
        assertEquals(3, book.getAvailableCopies());
    }

    @Test
    void returnLoan_WhenAlreadyReturned_ShouldThrowIllegalState() {
        Loan loan = new Loan(book, user, LocalDateTime.now().minusDays(5),
                LocalDateTime.now().plusDays(9), LoanStatus.RETURNED);
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> loanService.returnLoan(5L));

        verify(loanRepository, never()).save(any(Loan.class));
    }
}
