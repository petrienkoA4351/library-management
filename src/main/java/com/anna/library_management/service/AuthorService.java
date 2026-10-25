package com.anna.library_management.service;

import com.anna.library_management.entity.Author;
import java.util.List;
import java.util.Optional;

public interface AuthorService {
    List<Author> findAll();

    Optional<Author> findById(Long id);

    Author save(Author author);

    void deleteById(Long id);

    List<Author> searchByName(String name);
}
