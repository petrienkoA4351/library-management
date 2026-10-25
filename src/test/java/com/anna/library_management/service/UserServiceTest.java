package com.anna.library_management.service;

import com.anna.library_management.entity.User;
import com.anna.library_management.repository.UserRepository;
import com.anna.library_management.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@library.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Тест");
        testUser.setLastName("Пользователь");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        List<User> result = userService.findAll();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("test@library.com", result.get().getEmail());
    }

    @Test
    void save_ShouldDelegateToRepository() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.save(testUser);

        assertEquals("test@library.com", result.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteById_ShouldCallRepository() {
        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("test@library.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail("test@library.com");

        assertTrue(result.isPresent());
        assertEquals("test@library.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@library.com");
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        when(userRepository.findByEmail("nonexistent@library.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("nonexistent@library.com");

        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("nonexistent@library.com");
    }
}
