package com.anna.library_management.repository;

import com.anna.library_management.TestData;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Loan;
import com.anna.library_management.entity.LoanStatus;
import com.anna.library_management.entity.User;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void findByEmail_ShouldReturnUser() {
        userRepository.save(TestData.user("reader@library.com"));

        Optional<User> result = userRepository.findByEmail("reader@library.com");

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getFirstName());
    }

    @Test
    void findByEmail_WhenNotExists_ShouldReturnEmpty() {
        assertTrue(userRepository.findByEmail("nobody@library.com").isEmpty());
    }

    @Test
    void existsByEmail_ShouldCheckExistence() {
        userRepository.save(TestData.user("reader@library.com"));

        assertTrue(userRepository.existsByEmail("reader@library.com"));
        assertFalse(userRepository.existsByEmail("nobody@library.com"));
    }

    @Test
    void findByFirstNameOrLastName_ShouldSearchIgnoringCase() {
        userRepository.save(TestData.user("reader@library.com"));

        List<User> result = userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("TEST", "TEST");

        assertEquals(1, result.size());
    }

    @Test
    void findUsersWithActiveLoans_ShouldReturnOnlyUsersWithActiveLoans() {
        User active = userRepository.save(TestData.user("active@library.com"));
        User idle = userRepository.save(TestData.user("idle@library.com"));
        Book book = bookRepository.save(TestData.book("Book", "111-111", 1));
        loanRepository.save(new Loan(book, active, LocalDateTime.now(),
                LocalDateTime.now().plusDays(14), LoanStatus.ACTIVE));
        loanRepository.save(new Loan(book, idle, LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(6), LoanStatus.RETURNED));

        List<User> result = userRepository.findUsersWithActiveLoans();

        assertEquals(1, result.size());
        assertEquals("active@library.com", result.get(0).getEmail());
    }
}
