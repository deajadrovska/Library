package mk.finki.ukim.mk.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Authentication functionality
 * Tests complete authentication flow including registration and login
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CreateUserDto validCreateUserDto;
    private LoginUserDto validLoginUserDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        validCreateUserDto = new CreateUserDto(
                "testuser",
                "password123",
                "password123",
                "Test",
                "User",
                Role.ROLE_USER
        );

        validLoginUserDto = new LoginUserDto("testuser", "password123");
    }

    @Test
    void register_ShouldCreateUser_WhenValidData() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.surname", is("User")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        // Verify user was saved to database
        assert userRepository.count() == 1;
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assert passwordEncoder.matches("password123", savedUser.getPassword());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
        // Given
        CreateUserDto mismatchedPasswordDto = new CreateUserDto(
                "testuser",
                "password123",
                "differentpassword",
                "Test",
                "User",
                Role.ROLE_USER
        );

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchedPasswordDto)))
                .andExpect(status().isBadRequest());

        // Verify no user was saved
        assert userRepository.count() == 0;
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUsernameAlreadyExists() throws Exception {
        // Given - Create user first
        User existingUser = new User("testuser", passwordEncoder.encode("password"), "Existing", "User", Role.ROLE_USER);
        userRepository.save(existingUser);

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDto)))
                .andExpect(status().isBadRequest());

        // Verify only one user exists
        assert userRepository.count() == 1;
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given - Create user first
        User user = new User("testuser", passwordEncoder.encode("password123"), "Test", "User", Role.ROLE_USER);
        userRepository.save(user);

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    @Test
    void login_ShouldReturnNotFound_WhenInvalidUsername() throws Exception {
        // Given
        LoginUserDto invalidUserDto = new LoginUserDto("nonexistent", "password123");

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_ShouldReturnNotFound_WhenInvalidPassword() throws Exception {
        // Given - Create user first
        User user = new User("testuser", passwordEncoder.encode("password123"), "Test", "User", Role.ROLE_USER);
        userRepository.save(user);

        LoginUserDto invalidPasswordDto = new LoginUserDto("testuser", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPasswordDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerAndLogin_ShouldWorkTogether_WhenValidFlow() throws Exception {
        // Step 1: Register user
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDto)))
                .andExpect(status().isOk());

        // Step 2: Login with same credentials
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void register_ShouldCreateLibrarian_WhenLibrarianRole() throws Exception {
        // Given
        CreateUserDto librarianDto = new CreateUserDto(
                "librarian",
                "password123",
                "password123",
                "Lib",
                "Rarian",
                Role.ROLE_LIBRARIAN
        );

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(librarianDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ROLE_LIBRARIAN")));

        // Verify librarian was saved
        User savedLibrarian = userRepository.findByUsername("librarian").orElseThrow();
        assert savedLibrarian.getRole() == Role.ROLE_LIBRARIAN;
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmptyUsername() throws Exception {
        // Given
        CreateUserDto emptyUsernameDto = new CreateUserDto(
                "",
                "password123",
                "password123",
                "Test",
                "User",
                Role.ROLE_USER
        );

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUsernameDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmptyPassword() throws Exception {
        // Given
        CreateUserDto emptyPasswordDto = new CreateUserDto(
                "testuser",
                "",
                "",
                "Test",
                "User",
                Role.ROLE_USER
        );

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPasswordDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnNotFound_WhenEmptyCredentials() throws Exception {
        // Given
        LoginUserDto emptyCredentialsDto = new LoginUserDto("", "");

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyCredentialsDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticationEndpoints_ShouldHandleMalformedJson() throws Exception {
        // Test register with malformed JSON
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        // Test login with malformed JSON
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordEncoding_ShouldBeSecure_WhenUserRegistered() throws Exception {
        // When
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDto)))
                .andExpect(status().isOk());

        // Then
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assert !savedUser.getPassword().equals("password123"); // Password should be encoded
        assert passwordEncoder.matches("password123", savedUser.getPassword()); // But should match when checked
    }
}
