package com.anna.library_management.controller;

import com.anna.library_management.dto.BookDTO;
import com.anna.library_management.dto.CreateBookDTO;
import com.anna.library_management.entity.Author;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Category;
import com.anna.library_management.service.AuthorService;
import com.anna.library_management.service.BookService;
import com.anna.library_management.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private CategoryService categoryService;

    // Получить все книги
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<Book> books = bookService.findAll();
        List<BookDTO> bookDTOs = books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    // Получить книгу по ID
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.findById(id);
        if (book.isPresent()) {
            return ResponseEntity.ok(convertToDTO(book.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Создать новую книгу
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookDTO createBookDTO) {
        Optional<Author> author = authorService.findById(createBookDTO.getAuthorId());
        Optional<Category> category = categoryService.findById(createBookDTO.getCategoryId());
        if (author.isEmpty() || category.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Book savedBook = bookService.createBookWithAuthorAndCategory(
                    createBookDTO, author.get(), category.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Обновить книгу
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id,
                                              @Valid @RequestBody CreateBookDTO createBookDTO) {
        Optional<Book> existingBook = bookService.findById(id);
        if (existingBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Author> author = authorService.findById(createBookDTO.getAuthorId());
        Optional<Category> category = categoryService.findById(createBookDTO.getCategoryId());
        if (author.isEmpty() || category.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Book book = convertToEntity(createBookDTO, author.get(), category.get());
            book.setId(id);
            Book updatedBook = bookService.save(book);
            return ResponseEntity.ok(convertToDTO(updatedBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Удалить книгу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        Optional<Book> book = bookService.findById(id);
        if (book.isPresent()) {
            bookService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Поиск книг по названию
    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String title) {
        List<Book> books = bookService.findByTitleContaining(title);
        List<BookDTO> bookDTOs = books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    // Получить доступные книги
    @GetMapping("/available")
    public ResponseEntity<List<BookDTO>> getAvailableBooks() {
        List<Book> books = bookService.findAvailableBooks();
        List<BookDTO> bookDTOs = books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    // Получить книги автора
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BookDTO>> getBooksByAuthor(@PathVariable Long authorId) {
        List<BookDTO> bookDTOs = bookService.findByAuthorId(authorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    // Конвертация Entity в DTO
    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setPublicationDate(book.getPublicationDate());
        dto.setAvailableCopies(book.getAvailableCopies());
        if (book.getAuthor() != null) {
            dto.setAuthorId(book.getAuthor().getId());
            dto.setAuthorName(book.getAuthor().getFirstName() + " "
                    + book.getAuthor().getLastName());
        }
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        return dto;
    }

    // Конвертация DTO в Entity (автор и категория уже найдены по ID)
    private Book convertToEntity(CreateBookDTO dto, Author author, Category category) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setPublicationDate(dto.getPublicationDate());
        book.setAvailableCopies(dto.getAvailableCopies());
        book.setAuthor(author);
        book.setCategory(category);
        return book;
    }
}
