package com.anna.library_management.controller;

import com.anna.library_management.dto.CategoryDTO;
import com.anna.library_management.entity.Category;
import com.anna.library_management.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Получить все категории
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    // Получить категорию по ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(convertToDTO(category.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Создать категорию
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        Category category = new Category(dto.getName(), dto.getDescription());
        Category saved = categoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    // Обновить категорию
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
                                                      @Valid @RequestBody CategoryDTO dto) {
        Optional<Category> existing = categoryService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Category category = existing.get();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return ResponseEntity.ok(convertToDTO(categoryService.save(category)));
    }

    // Удалить категорию
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (categoryService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CategoryDTO convertToDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription());
    }
}
