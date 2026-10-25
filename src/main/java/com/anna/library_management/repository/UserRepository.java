package com.anna.library_management.repository;

import com.anna.library_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Поиск по email
    Optional<User> findByEmail(String email);

    // Поиск по имени или фамилии
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Проверка существования пользователя по email
    boolean existsByEmail(String email);

    // Поиск пользователей с активными займами
    @Query("SELECT DISTINCT u FROM User u JOIN u.loans l "
            + "WHERE l.status = com.anna.library_management.entity.LoanStatus.ACTIVE")
    List<User> findUsersWithActiveLoans();
}
