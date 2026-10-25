package com.anna.library_management.service.impl;

import com.anna.library_management.entity.Book;
import com.anna.library_management.repository.BookRepository;
import com.anna.library_management.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Book> findByAuthorId(Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }

    // Дополнительные методы
    public List<Book> findAvailableBooks() {
        return bookRepository.findByAvailableCopiesGreaterThan(0);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public List<Book> findOverdueBooks() {
        return bookRepository.findOverdueBooks();
    }
}
