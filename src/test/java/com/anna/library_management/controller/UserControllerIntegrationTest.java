package com.anna.library_management.controller;

import com.anna.library_management.TestData;
import com.anna.library_management.entity.User;
import com.anna.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String userJson(String email, String password) {
        return """
                {"email":"%s","password":"%s","firstName":"Ivan","lastName":"Ivanov"}
                """.formatted(email, password);
    }

    @Test
    void createUser_ShouldStoreEncodedPasswordAndHideIt() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("new@library.com", "secret123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@library.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User saved = userRepository.findByEmail("new@library.com").orElseThrow();
        assertNotEquals("secret123", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"));
        assertTrue(passwordEncoder.matches("secret123", saved.getPassword()));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        userRepository.save(TestData.user("exists@library.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("exists@library.com", "secret123")))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("not-an-email", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void getUserByEmail_ShouldReturnUserWithoutPassword() throws Exception {
        userRepository.save(TestData.user("reader@library.com"));

        mockMvc.perform(get("/api/users/email/{email}", "reader@library.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("reader@library.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        User user = userRepository.save(TestData.user("delete@library.com"));

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(user.getId()).isEmpty());
    }
}
