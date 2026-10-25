package com.anna.library_management.repository;

import com.anna.library_management.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByName_ShouldReturnCategory() {
        categoryRepository.save(new Category("Fiction", "Novels"));

        Optional<Category> result = categoryRepository.findByName("Fiction");

        assertTrue(result.isPresent());
        assertEquals("Novels", result.get().getDescription());
    }

    @Test
    void findByName_WhenNotExists_ShouldReturnEmpty() {
        assertTrue(categoryRepository.findByName("Unknown").isEmpty());
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldFindPartialMatch() {
        categoryRepository.save(new Category("Science fiction", "Sci-fi"));
        categoryRepository.save(new Category("History", "Historical books"));

        List<Category> result = categoryRepository.findByNameContainingIgnoreCase("FICTION");

        assertEquals(1, result.size());
        assertEquals("Science fiction", result.get(0).getName());
    }

    @Test
    void existsByName_ShouldCheckExistence() {
        categoryRepository.save(new Category("Fiction", "Novels"));

        assertTrue(categoryRepository.existsByName("Fiction"));
        assertFalse(categoryRepository.existsByName("Unknown"));
    }
}
