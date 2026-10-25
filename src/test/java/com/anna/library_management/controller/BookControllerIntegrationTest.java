package com.anna.library_management.controller;

import com.anna.library_management.TestData;
import com.anna.library_management.entity.Author;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Category;
import com.anna.library_management.repository.AuthorRepository;
import com.anna.library_management.repository.BookRepository;
import com.anna.library_management.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockMvc mockMvc;
    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        // MockMvc без фильтров безопасности: здесь проверяется только логика контроллера
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        author = authorRepository.save(new Author("Leo", "Tolstoy"));
        category = categoryRepository.save(new Category("Fiction", "Novels"));
    }

    private Book saveBook(String title, String isbn, int copies) {
        Book book = TestData.book(title, isbn, copies);
        book.setAuthor(author);
        book.setCategory(category);
        return bookRepository.save(book);
    }

    private String bookJson(String title, int copies, Long authorId, Long categoryId) {
        return """
                {"title":"%s","isbn":"978-5-17-123456-7","publicationDate":"1869-01-01",
                 "availableCopies":%d,"authorId":%d,"categoryId":%d}
                """.formatted(title, copies, authorId, categoryId);
    }

    @Test
    void getAllBooks_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllBooks_ShouldReturnSavedBooks() throws Exception {
        saveBook("War and Peace", "111-111", 5);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("War and Peace"))
                .andExpect(jsonPath("$[0].authorName").value("Leo Tolstoy"))
                .andExpect(jsonPath("$[0].categoryName").value("Fiction"));
    }

    @Test
    void createBook_ShouldReturnCreatedBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson("War and Peace", 5, author.getId(), category.getId())))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("War and Peace"))
                .andExpect(jsonPath("$.isbn").value("978-5-17-123456-7"))
                .andExpect(jsonPath("$.authorId").value(author.getId()))
                .andExpect(jsonPath("$.categoryId").value(category.getId()));
    }

    @Test
    void createBook_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson("", -1, author.getId(), category.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.availableCopies").exists());
    }

    @Test
    void createBook_WithUnknownAuthor_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson("War and Peace", 5, 9999L, category.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookById_WhenBookExists_ShouldReturnBook() throws Exception {
        Book book = saveBook("War and Peace", "111-111", 5);

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(book.getId()))
                .andExpect(jsonPath("$.title").value("War and Peace"));
    }

    @Test
    void getBookById_WhenBookNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBook_ShouldReturnUpdatedBook() throws Exception {
        Book book = saveBook("War and Peace", "111-111", 5);

        mockMvc.perform(put("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson("War and Peace (updated)", 7,
                                author.getId(), category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book.getId()))
                .andExpect(jsonPath("$.title").value("War and Peace (updated)"))
                .andExpect(jsonPath("$.availableCopies").value(7));
    }

    @Test
    void updateBook_WhenBookNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/books/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson("Any", 1, author.getId(), category.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_ShouldReturnNoContent() throws Exception {
        Book book = saveBook("War and Peace", "111-111", 5);

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());

        assertFalse(bookRepository.findById(book.getId()).isPresent());
    }

    @Test
    void deleteBook_WhenBookNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchBooks_ShouldReturnMatchingBooks() throws Exception {
        saveBook("War and Peace", "111-111", 5);
        saveBook("Anna Karenina", "222-222", 3);

        mockMvc.perform(get("/api/books/search").param("title", "war"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("War and Peace"));
    }

    @Test
    void getAvailableBooks_ShouldSkipBooksWithoutCopies() throws Exception {
        saveBook("Available", "111-111", 5);
        saveBook("Not available", "222-222", 0);

        mockMvc.perform(get("/api/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Available"));
    }
}
