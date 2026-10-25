package com.anna.library_management.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class AuthorDTO {

    private Long id;

    @NotBlank(message = "Имя автора обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия автора обязательна")
    private String lastName;

    private LocalDate birthDate;

    private String biography;

    public AuthorDTO() {}

    public AuthorDTO(Long id, String firstName, String lastName, LocalDate birthDate, String biography) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.biography = biography;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
}
