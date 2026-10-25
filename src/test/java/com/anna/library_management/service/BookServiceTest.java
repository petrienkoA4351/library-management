package com.anna.library_management.service;

import com.anna.library_management.dto.CreateBookDTO;
import com.anna.library_management.entity.Author;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Category;
import com.anna.library_management.repository.BookRepository;
import com.anna.library_management.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private Author testAuthor;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testAuthor = new Author("Лев", "Толстой");
        testAuthor.setId(1L);

        testCategory = new Category("Художественная литература", "Романы");
        testCategory.setId(1L);

        testBook = new Book("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5);
        testBook.setId(1L);
        testBook.setAuthor(testAuthor);
        testBook.setCategory(testCategory);
    }

    @Test
    void findAll_ShouldReturnAllBooks() {
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        List<Book> actualBooks = bookService.findAll();

        assertEquals(1, actualBooks.size());
        assertEquals("Война и мир", actualBooks.get(0).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    void findById_WhenBookExists_ShouldReturnBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        Optional<Book> result = bookService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Война и мир", result.get().getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    void findById_WhenBookNotExists_ShouldReturnEmpty() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findById(999L);

        assertFalse(result.isPresent());
        verify(bookRepository).findById(999L);
    }

    @Test
    void save_ShouldReturnSavedBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book result = bookService.save(testBook);

        assertEquals("Война и мир", result.getTitle());
        verify(bookRepository).save(testBook);
    }

    @Test
    void deleteById_ShouldCallRepository() {
        doNothing().when(bookRepository).deleteById(1L);

        bookService.deleteById(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void findByTitleContaining_ShouldReturnMatchingBooks() {
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByTitleContainingIgnoreCase("война")).thenReturn(expectedBooks);

        List<Book> result = bookService.findByTitleContaining("война");

        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
        verify(bookRepository).findByTitleContainingIgnoreCase("война");
    }

    @Test
    void findByAuthorId_ShouldReturnBooksByAuthor() {
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByAuthorId(1L)).thenReturn(expectedBooks);

        List<Book> result = bookService.findByAuthorId(1L);

        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
        verify(bookRepository).findByAuthorId(1L);
    }

    @Test
    void findAvailableBooks_ShouldSearchBooksWithPositiveCopies() {
        when(bookRepository.findByAvailableCopiesGreaterThan(0))
                .thenReturn(Arrays.asList(testBook));

        List<Book> result = bookService.findAvailableBooks();

        assertEquals(1, result.size());
        verify(bookRepository).findByAvailableCopiesGreaterThan(0);
    }

    @Test
    void findByIsbn_ShouldReturnBook() {
        when(bookRepository.findByIsbn("978-5-17-123456-7")).thenReturn(Optional.of(testBook));

        Optional<Book> result = bookService.findByIsbn("978-5-17-123456-7");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void createBookWithAuthorAndCategory_ShouldFillAllFields() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Евгений Онегин");
        dto.setIsbn("978-5-17-123457-4");
        dto.setPublicationDate(LocalDate.of(1833, 1, 1));
        dto.setAvailableCopies(3);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.createBookWithAuthorAndCategory(dto, testAuthor, testCategory);

        assertEquals("Евгений Онегин", result.getTitle());
        assertEquals(3, result.getAvailableCopies());
        assertSame(testAuthor, result.getAuthor());
        assertSame(testCategory, result.getCategory());
        verify(bookRepository).save(any(Book.class));
    }
}
