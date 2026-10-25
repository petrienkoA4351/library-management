package com.anna.library_management.controller;

import com.anna.library_management.dto.AuthorDTO;
import com.anna.library_management.entity.Author;
import com.anna.library_management.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    // Получить всех авторов
    @GetMapping
    public ResponseEntity<List<AuthorDTO>> getAllAuthors() {
        List<AuthorDTO> authors = authorService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(authors);
    }

    // Получить автора по ID
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable Long id) {
        Optional<Author> author = authorService.findById(id);
        if (author.isPresent()) {
            return ResponseEntity.ok(convertToDTO(author.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Поиск авторов по имени или фамилии
    @GetMapping("/search")
    public ResponseEntity<List<AuthorDTO>> searchAuthors(@RequestParam String name) {
        List<AuthorDTO> authors = authorService.searchByName(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(authors);
    }

    // Создать автора
    @PostMapping
    public ResponseEntity<AuthorDTO> createAuthor(@Valid @RequestBody AuthorDTO dto) {
        Author author = convertToEntity(dto);
        author.setId(null);
        Author saved = authorService.save(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    // Обновить автора
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(@PathVariable Long id,
                                                  @Valid @RequestBody AuthorDTO dto) {
        Optional<Author> existing = authorService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Author author = existing.get();
        author.setFirstName(dto.getFirstName());
        author.setLastName(dto.getLastName());
        author.setBirthDate(dto.getBirthDate());
        author.setBiography(dto.getBiography());
        return ResponseEntity.ok(convertToDTO(authorService.save(author)));
    }

    // Удалить автора (вместе с его книгами, так задано каскадом)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        if (authorService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        authorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private AuthorDTO convertToDTO(Author author) {
        return new AuthorDTO(author.getId(), author.getFirstName(), author.getLastName(),
                author.getBirthDate(), author.getBiography());
    }

    private Author convertToEntity(AuthorDTO dto) {
        Author author = new Author(dto.getFirstName(), dto.getLastName());
        author.setBirthDate(dto.getBirthDate());
        author.setBiography(dto.getBiography());
        return author;
    }
}
