package com.anna.library_management.service;

import com.anna.library_management.entity.Book;
import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> findAll();

    Optional<Book> findById(Long id);

    Book save(Book book);

    void deleteById(Long id);

    List<Book> findByTitleContaining(String title);

    List<Book> findByAuthorId(Long authorId);
}
