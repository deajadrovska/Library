package mk.finki.ukim.mk.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.repository.UserRepository;
import mk.finki.ukim.mk.library.config.IntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {LibraryApplication.class, IntegrationTestConfig.class},
    properties = {
            "spring.profiles.active=integration-test",
            "spring.main.allow-bean-definition-overriding=true"
        })
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up database
        userRepository.deleteAll();

        // Create test user
        testUser = new User("testuser", passwordEncoder.encode("password123"), "Test", "User", Role.ROLE_USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void register_ShouldCreateUser_WhenValidData() throws Exception {
        // Given
        CreateUserDto createUserDto = new CreateUserDto(
                "newuser",
                "password123",
                "password123",
                "New",
                "User",
                Role.ROLE_USER
        );

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.name", is("New")))
                .andExpect(jsonPath("$.surname", is("User")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        // Verify user was saved to database
        assert userRepository.count() == 2;
        assert userRepository.findByUsername("newuser").isPresent();
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
        // Given
        CreateUserDto createUserDto = new CreateUserDto(
                "newuser",
                "password123",
                "differentpassword", // Different password
                "New",
                "User",
                Role.ROLE_USER
        );

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest());

        // Verify user was not saved
        assert userRepository.count() == 1;
        assert userRepository.findByUsername("newuser").isEmpty();
    }


    @Test
    void register_ShouldCreateLibrarian_WhenLibrarianRole() throws Exception {
        // Given
        CreateUserDto createUserDto = new CreateUserDto(
                "librarian",
                "password123",
                "password123",
                "Library",
                "Admin",
                Role.ROLE_LIBRARIAN
        );

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("librarian")))
                .andExpect(jsonPath("$.name", is("Library")))
                .andExpect(jsonPath("$.surname", is("Admin")))
                .andExpect(jsonPath("$.role", is("ROLE_LIBRARIAN")));

        // Verify librarian was saved to database
        assert userRepository.count() == 2;
        User savedLibrarian = userRepository.findByUsername("librarian").orElseThrow();
        assert savedLibrarian.getRole() == Role.ROLE_LIBRARIAN;
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given
        LoginUserDto loginUserDto = new LoginUserDto("testuser", "password123");

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void login_ShouldReturnNotFound_WhenInvalidUsername() throws Exception {
        // Given
        LoginUserDto loginUserDto = new LoginUserDto("nonexistent", "password123");

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_ShouldReturnNotFound_WhenInvalidPassword() throws Exception {
        // Given
        LoginUserDto loginUserDto = new LoginUserDto("testuser", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenMalformedJson() throws Exception {
        // Given - malformed JSON
        String malformedJson = "{invalid json}";

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenMalformedJson() throws Exception {
        // Given - malformed JSON
        String malformedJson = "{invalid json}";

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }



}
