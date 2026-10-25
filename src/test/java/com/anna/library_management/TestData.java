package com.anna.library_management;

import com.anna.library_management.entity.Book;
import com.anna.library_management.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Фабричные методы для создания тестовых объектов
public final class TestData {

    private TestData() {
    }

    public static User user(String email) {
        User user = new User(email, "password123", "Test", "User");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public static Book book(String title, String isbn, int copies) {
        return new Book(title, isbn, LocalDate.of(2000, 1, 1), copies);
    }
}
