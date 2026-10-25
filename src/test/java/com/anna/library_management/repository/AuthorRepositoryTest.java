package com.anna.library_management.repository;

import com.anna.library_management.TestData;
import com.anna.library_management.entity.Author;
import com.anna.library_management.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private Author tolstoy;
    private Author pushkin;

    @BeforeEach
    void setUp() {
        tolstoy = authorRepository.save(new Author("Leo", "Tolstoy"));
        pushkin = authorRepository.save(new Author("Alexander", "Pushkin"));
    }

    @Test
    void findByLastNameContainingIgnoreCase_ShouldFindAuthor() {
        List<Author> result = authorRepository.findByLastNameContainingIgnoreCase("TOLST");

        assertEquals(1, result.size());
        assertEquals("Leo", result.get(0).getFirstName());
    }

    @Test
    void findByFirstNameOrLastName_ShouldSearchByBothFields() {
        List<Author> byFirstName = authorRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("alex", "alex");
        List<Author> byLastName = authorRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("push", "push");

        assertEquals(1, byFirstName.size());
        assertEquals(1, byLastName.size());
        assertEquals(pushkin.getId(), byFirstName.get(0).getId());
    }

    @Test
    void findAuthorsWithBooks_ShouldReturnOnlyAuthorsWithBooks() {
        Book book = TestData.book("War and Peace", "111-111", 5);
        book.setAuthor(tolstoy);
        bookRepository.save(book);

        List<Author> result = authorRepository.findAuthorsWithBooks();

        assertEquals(1, result.size());
        assertEquals("Tolstoy", result.get(0).getLastName());
    }

    @Test
    void countBooksByAuthorId_ShouldCountBooks() {
        for (int i = 0; i < 2; i++) {
            Book book = TestData.book("Book " + i, "isbn-" + i, 1);
            book.setAuthor(tolstoy);
            bookRepository.save(book);
        }

        assertEquals(2L, authorRepository.countBooksByAuthorId(tolstoy.getId()));
        assertEquals(0L, authorRepository.countBooksByAuthorId(pushkin.getId()));
    }
}
