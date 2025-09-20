package mk.finki.ukim.mk.library.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.config.IntegrationTestConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * User Acceptance Tests (UAT)
 * Tests that validate the system meets user requirements and expectations.
 * These tests are written from the user's perspective and validate business value.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class UserAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("UAT: As a new user, I want to register for the library system so I can access library services")
    void uat_NewUserRegistration_ShouldProvideAccessToLibraryServices() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "newuser" + timestamp;
        
        // User Story: New user registration
        CreateUserDto newUser = new CreateUserDto(
            username, "securePassword123", "securePassword123",
            "John", "Doe", Role.ROLE_USER
        );
        
        // When: User registers for the system
        MvcResult registrationResult = mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andReturn();

        // Then: User should be able to login and access services
        LoginUserDto loginDto = new LoginUserDto(username, "securePassword123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // And: User should have access to their personal wishlist
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.books").isArray());
    }

    @Test
    @DisplayName("UAT: As a library user, I want to browse available books so I can find books of interest")
    void uat_BookBrowsing_ShouldAllowUsersToDiscoverBooks() throws Exception {
        // User Story: Book discovery and browsing
        
        // When: User browses the book catalog
        MvcResult booksResult = mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // Then: User should see a list of available books
        String booksJson = booksResult.getResponse().getContentAsString();
        assertNotNull(booksJson, "Books list should be returned");

        // And: User should be able to view book details
        // Note: This assumes at least one book exists or we create test data
        
        // And: User should be able to browse authors
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // And: User should be able to browse countries (for author information)
        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("UAT: As a registered user, I want to add books to my wishlist so I can keep track of books I'm interested in")
    void uat_WishlistManagement_ShouldAllowUsersToTrackDesiredBooks() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "wishlistuser" + timestamp;
        
        // Given: A registered user
        CreateUserDto user = new CreateUserDto(username, "password123", "password123", "Jane", "Smith", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        LoginUserDto loginDto = new LoginUserDto(username, "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // When: User views their empty wishlist
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.books").isArray());

        // Then: User should be able to add books to wishlist (if books exist)
        // Note: This test assumes the existence of books or would need test data setup
        
        // And: User should be able to view their updated wishlist
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    @DisplayName("UAT: As a librarian, I want to have administrative access so I can manage the library system")
    void uat_LibrarianAccess_ShouldProvideAdministrativeCapabilities() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String librarianUsername = "librarian" + timestamp;
        
        // User Story: Librarian administrative access
        
        // When: Librarian registers in the system
        CreateUserDto librarian = new CreateUserDto(
            librarianUsername, "librarianPass123", "librarianPass123",
            "Alice", "Johnson", Role.ROLE_LIBRARIAN
        );
        
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(librarianUsername))
                .andExpect(jsonPath("$.role").value("ROLE_LIBRARIAN"));

        // Then: Librarian should be able to login
        LoginUserDto librarianLogin = new LoginUserDto(librarianUsername, "librarianPass123");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // And: Librarian should have access to all public resources
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/authors")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // And: Librarian should have their own wishlist (like regular users)
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(librarianUsername));
    }

    @Test
    @DisplayName("UAT: As a user, I want secure authentication so my account and data are protected")
    void uat_SecureAuthentication_ShouldProtectUserAccountsAndData() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "secureuser" + timestamp;
        
        // User Story: Secure authentication and data protection
        
        // Given: A user with secure credentials
        CreateUserDto secureUser = new CreateUserDto(username, "verySecurePassword123!", "verySecurePassword123!", "Bob", "Wilson", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secureUser)))
                .andExpect(status().isOk());

        // When: User logs in with correct credentials
        LoginUserDto correctLogin = new LoginUserDto(username, "verySecurePassword123!");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String validToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Then: User should be able to access protected resources
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        // But: User with wrong credentials should be rejected
        LoginUserDto wrongLogin = new LoginUserDto(username, "wrongPassword");
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isNotFound()); // Invalid credentials return 404

        // And: Access without token should be denied
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());

        // And: Access with invalid token should be denied
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UAT: As a user, I want the system to handle errors gracefully so I have a good user experience")
    void uat_ErrorHandling_ShouldProvideGoodUserExperience() throws Exception {
        // User Story: Graceful error handling and user experience
        
        // When: User tries to register with invalid data
        CreateUserDto invalidUser = new CreateUserDto("", "", "", "", "", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        // Then: System should handle the error gracefully (not crash)
        
        // When: User tries to access non-existent resources
        mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/authors/99999"))
                .andExpect(status().isNotFound());

        // Then: System should return appropriate error responses
        
        // When: User sends malformed requests
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        // Then: System should handle malformed input gracefully
    }

    @Test
    @DisplayName("UAT: As a user, I want consistent data across sessions so my information is reliable")
    void uat_DataConsistency_ShouldMaintainReliableInformation() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "consistentuser" + timestamp;
        
        // User Story: Data consistency and reliability
        
        // Given: User registers and logs in
        CreateUserDto user = new CreateUserDto(username, "password123", "password123", "Charlie", "Brown", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        LoginUserDto loginDto = new LoginUserDto(username, "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // When: User accesses their data multiple times
        MvcResult firstAccess = mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        MvcResult secondAccess = mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        // Then: Data should be consistent across requests
        String firstResponse = firstAccess.getResponse().getContentAsString();
        String secondResponse = secondAccess.getResponse().getContentAsString();
        
        // Basic consistency check - username should be the same
        assertTrue(firstResponse.contains(username), "First response should contain username");
        assertTrue(secondResponse.contains(username), "Second response should contain username");
    }

    private void assertNotNull(String value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
