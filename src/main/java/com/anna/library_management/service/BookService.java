package com.anna.library_management.service;

import com.anna.library_management.dto.CreateBookDTO;
import com.anna.library_management.entity.Author;
import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.Category;
import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> findAll();

    Optional<Book> findById(Long id);

    Book save(Book book);

    void deleteById(Long id);

    List<Book> findByTitleContaining(String title);

    List<Book> findByAuthorId(Long authorId);

    List<Book> findAvailableBooks();

    Optional<Book> findByIsbn(String isbn);

    List<Book> findOverdueBooks();

    Book createBookWithAuthorAndCategory(CreateBookDTO dto, Author author, Category category);
}
