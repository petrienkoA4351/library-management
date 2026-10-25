package com.anna.library_management.repository;

import com.anna.library_management.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Поиск по названию (частичное совпадение)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Поиск по ISBN
    Optional<Book> findByIsbn(String isbn);

    // Поиск по автору
    List<Book> findByAuthorId(Long authorId);

    // Поиск по категории
    List<Book> findByCategoryId(Long categoryId);

    // Поиск доступных книг
    List<Book> findByAvailableCopiesGreaterThan(Integer copies);

    // Кастомный запрос для поиска по названию и автору
    @Query("SELECT b FROM Book b WHERE b.title LIKE CONCAT('%', :title, '%') "
            + "AND b.author.lastName = :authorLastName")
    List<Book> findByTitleAndAuthor(@Param("title") String title,
                                    @Param("authorLastName") String authorLastName);

    // Поиск книг с истекшим сроком возврата
    @Query("SELECT DISTINCT l.book FROM Loan l WHERE l.dueDate < CURRENT_TIMESTAMP "
            + "AND l.status = com.anna.library_management.entity.LoanStatus.ACTIVE")
    List<Book> findOverdueBooks();
}
