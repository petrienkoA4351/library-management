package com.anna.library_management.repository;

import com.anna.library_management.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Поиск по имени или фамилии
    List<Author> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Поиск по фамилии
    List<Author> findByLastNameContainingIgnoreCase(String lastName);

    // Поиск авторов с книгами
    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b")
    List<Author> findAuthorsWithBooks();

    // Подсчет количества книг автора
    @Query("SELECT COUNT(b) FROM Book b WHERE b.author.id = :authorId")
    Long countBooksByAuthorId(@Param("authorId") Long authorId);
}
