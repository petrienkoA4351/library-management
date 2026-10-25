package com.anna.library_management.repository;

import com.anna.library_management.TestData;
import com.anna.library_management.entity.Author;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Category;
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
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        author = authorRepository.save(new Author("Leo", "Tolstoy"));
        category = categoryRepository.save(new Category("Fiction", "Novels"));
    }

    private Book saveBook(String title, String isbn, int copies) {
        Book book = TestData.book(title, isbn, copies);
        book.setAuthor(author);
        book.setCategory(category);
        return bookRepository.save(book);
    }

    @Test
    void findByIsbn_ShouldReturnBook() {
        saveBook("War and Peace", "111-111", 5);

        Optional<Book> result = bookRepository.findByIsbn("111-111");

        assertTrue(result.isPresent());
        assertEquals("War and Peace", result.get().getTitle());
    }

    @Test
    void findByIsbn_WhenNotExists_ShouldReturnEmpty() {
        assertTrue(bookRepository.findByIsbn("000-000").isEmpty());
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldIgnoreCase() {
        saveBook("War and Peace", "111-111", 5);
        saveBook("Anna Karenina", "222-222", 3);

        List<Book> result = bookRepository.findByTitleContainingIgnoreCase("war");

        assertEquals(1, result.size());
        assertEquals("War and Peace", result.get(0).getTitle());
    }

    @Test
    void findByAuthorId_ShouldReturnBooksOfAuthor() {
        saveBook("War and Peace", "111-111", 5);
        Author other = authorRepository.save(new Author("Alexander", "Pushkin"));
        Book foreign = TestData.book("Eugene Onegin", "333-333", 2);
        foreign.setAuthor(other);
        bookRepository.save(foreign);

        List<Book> result = bookRepository.findByAuthorId(author.getId());

        assertEquals(1, result.size());
        assertEquals("War and Peace", result.get(0).getTitle());
    }

    @Test
    void findByCategoryId_ShouldReturnBooksOfCategory() {
        saveBook("War and Peace", "111-111", 5);

        List<Book> result = bookRepository.findByCategoryId(category.getId());

        assertEquals(1, result.size());
    }

    @Test
    void findByAvailableCopiesGreaterThan_ShouldSkipBooksWithoutCopies() {
        saveBook("Available", "111-111", 5);
        saveBook("Not available", "222-222", 0);

        List<Book> result = bookRepository.findByAvailableCopiesGreaterThan(0);

        assertEquals(1, result.size());
        assertEquals("Available", result.get(0).getTitle());
    }

    @Test
    void findByTitleAndAuthor_ShouldFilterByTitleAndAuthorLastName() {
        saveBook("War and Peace", "111-111", 5);

        assertEquals(1, bookRepository.findByTitleAndAuthor("War", "Tolstoy").size());
        assertTrue(bookRepository.findByTitleAndAuthor("War", "Pushkin").isEmpty());
    }

    @Test
    void findOverdueBooks_ShouldReturnBooksWithExpiredActiveLoans() {
        Book overdue = saveBook("Overdue", "111-111", 1);
        Book returned = saveBook("Returned", "222-222", 1);
        User user = userRepository.save(TestData.user("reader@library.com"));

        loanRepository.save(new Loan(overdue, user, LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(10), LoanStatus.ACTIVE));
        loanRepository.save(new Loan(returned, user, LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(10), LoanStatus.RETURNED));

        List<Book> result = bookRepository.findOverdueBooks();

        assertEquals(1, result.size());
        assertEquals("Overdue", result.get(0).getTitle());
    }
}
