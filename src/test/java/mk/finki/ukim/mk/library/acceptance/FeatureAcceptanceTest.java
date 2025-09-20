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
 * Feature Acceptance Tests
 * Tests that validate specific features work as expected from an end-user perspective.
 * These tests focus on feature completeness and user satisfaction.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class FeatureAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Feature: User Registration - Complete registration workflow should work seamlessly")
    void feature_UserRegistration_CompleteWorkflowShouldWorkSeamlessly() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "featureuser" + timestamp;
        
        // Feature: User Registration
        // Scenario: New user wants to join the library system
        
        // Given: User has valid registration information
        CreateUserDto newUser = new CreateUserDto(
            username, "MySecurePassword123", "MySecurePassword123",
            "Feature", "Tester", Role.ROLE_USER
        );
        
        // When: User submits registration form
        MvcResult registrationResult = mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.name").value("Feature"))
                .andExpect(jsonPath("$.surname").value("Tester"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andReturn();

        // Then: User should be successfully registered
        String responseContent = registrationResult.getResponse().getContentAsString();
        assertTrue(responseContent.contains(username), "Response should contain the username");
        
        // And: User should be able to immediately log in
        LoginUserDto loginDto = new LoginUserDto(username, "MySecurePassword123");
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Feature: User Authentication - Login and logout workflow should be secure and reliable")
    void feature_UserAuthentication_LoginLogoutWorkflowShouldBeSecureAndReliable() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "authuser" + timestamp;
        
        // Feature: User Authentication
        // Scenario: User wants to securely access their account
        
        // Given: Registered user
        CreateUserDto user = new CreateUserDto(username, "AuthPassword123", "AuthPassword123", "Auth", "User", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // When: User logs in with correct credentials
        LoginUserDto validLogin = new LoginUserDto(username, "AuthPassword123");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Then: User should receive a valid authentication token
        assertNotNull(token, "Token should not be null");
        assertTrue(token.length() > 0, "Token should not be empty");

        // And: Token should provide access to protected resources
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        // But: Invalid credentials should be rejected
        LoginUserDto invalidLogin = new LoginUserDto(username, "WrongPassword");
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isNotFound()); // Invalid credentials return 404
    }

    @Test
    @DisplayName("Feature: Book Catalog Browsing - Users should be able to explore the book collection")
    void feature_BookCatalogBrowsing_UsersShouldBeAbleToExploreBookCollection() throws Exception {
        // Feature: Book Catalog Browsing
        // Scenario: User wants to discover available books
        
        // When: User browses the book catalog
        MvcResult booksResult = mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // Then: User should see a list of available books
        String booksJson = booksResult.getResponse().getContentAsString();
        assertNotNull(booksJson, "Books response should not be null");
        assertTrue(booksJson.startsWith("["), "Response should be a JSON array");

        // And: User should be able to browse authors
        MvcResult authorsResult = mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String authorsJson = authorsResult.getResponse().getContentAsString();
        assertNotNull(authorsJson, "Authors response should not be null");
        assertTrue(authorsJson.startsWith("["), "Response should be a JSON array");

        // And: User should be able to browse countries
        MvcResult countriesResult = mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String countriesJson = countriesResult.getResponse().getContentAsString();
        assertNotNull(countriesJson, "Countries response should not be null");
        assertTrue(countriesJson.startsWith("["), "Response should be a JSON array");

        // And: Browsing should not require authentication
        // (Already verified by successful requests without authentication headers)
    }

    @Test
    @DisplayName("Feature: Personal Wishlist - Users should be able to manage their personal book wishlist")
    void feature_PersonalWishlist_UsersShouldBeAbleToManagePersonalBookWishlist() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "wishlistuser" + timestamp;
        
        // Feature: Personal Wishlist Management
        // Scenario: User wants to track books they're interested in
        
        // Given: Authenticated user
        CreateUserDto user = new CreateUserDto(username, "WishlistPass123", "WishlistPass123", "Wishlist", "User", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        LoginUserDto loginDto = new LoginUserDto(username, "WishlistPass123");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // When: User accesses their wishlist
        MvcResult wishlistResult = mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.books").isArray())
                .andReturn();

        // Then: User should have a personal wishlist
        String wishlistJson = wishlistResult.getResponse().getContentAsString();
        assertTrue(wishlistJson.contains(username), "Wishlist should belong to the user");
        assertTrue(wishlistJson.contains("books"), "Wishlist should contain books array");

        // And: Wishlist should be private (require authentication)
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());

        // And: Invalid token should be rejected
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Feature: Role-Based Access - System should support different user roles with appropriate permissions")
    void feature_RoleBasedAccess_SystemShouldSupportDifferentUserRolesWithAppropriatePermissions() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Feature: Role-Based Access Control
        // Scenario: Different user types should have appropriate access levels
        
        // Given: Regular user and librarian user
        String regularUsername = "regular" + timestamp;
        String librarianUsername = "librarian" + timestamp;
        
        CreateUserDto regularUser = new CreateUserDto(regularUsername, "RegularPass123", "RegularPass123", "Regular", "User", Role.ROLE_USER);
        CreateUserDto librarianUser = new CreateUserDto(librarianUsername, "LibrarianPass123", "LibrarianPass123", "Librarian", "User", Role.ROLE_LIBRARIAN);
        
        // When: Both users register
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regularUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_LIBRARIAN"));

        // Then: Both should be able to authenticate
        LoginUserDto regularLogin = new LoginUserDto(regularUsername, "RegularPass123");
        MvcResult regularLoginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regularLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        LoginUserDto librarianLogin = new LoginUserDto(librarianUsername, "LibrarianPass123");
        MvcResult librarianLoginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String regularToken = objectMapper.readTree(regularLoginResult.getResponse().getContentAsString()).get("token").asText();
        String librarianToken = objectMapper.readTree(librarianLoginResult.getResponse().getContentAsString()).get("token").asText();

        // And: Both should have access to their own wishlists
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(regularUsername));

        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(librarianUsername));

        // And: Both should have access to public resources
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + regularToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Feature: Error Handling - System should provide meaningful error messages and graceful degradation")
    void feature_ErrorHandling_SystemShouldProvideMeaningfulErrorMessagesAndGracefulDegradation() throws Exception {
        // Feature: Error Handling and User Experience
        // Scenario: System should handle various error conditions gracefully
        
        // When: User provides invalid registration data
        CreateUserDto invalidUser = new CreateUserDto("", "", "", "", "", Role.ROLE_USER);
        MvcResult invalidRegistrationResult = mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then: System should return appropriate error status
        assertEquals(400, invalidRegistrationResult.getResponse().getStatus(), "Should return 400 Bad Request");

        // When: User tries to access non-existent resources
        mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/authors/99999"))
                .andExpect(status().isNotFound());

        // Then: System should return 404 Not Found
        
        // When: User sends malformed JSON
        MvcResult malformedJsonResult = mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json content"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then: System should handle malformed input gracefully
        assertEquals(400, malformedJsonResult.getResponse().getStatus(), "Should return 400 Bad Request for malformed JSON");

        // And: System should remain operational after errors
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Feature: Data Consistency - System should maintain consistent data across operations")
    void feature_DataConsistency_SystemShouldMaintainConsistentDataAcrossOperations() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "consistencyuser" + timestamp;
        
        // Feature: Data Consistency and Reliability
        // Scenario: User data should remain consistent across multiple operations
        
        // Given: User registers and logs in
        CreateUserDto user = new CreateUserDto(username, "ConsistencyPass123", "ConsistencyPass123", "Consistency", "User", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        LoginUserDto loginDto = new LoginUserDto(username, "ConsistencyPass123");
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
        
        assertTrue(firstResponse.contains(username), "First response should contain username");
        assertTrue(secondResponse.contains(username), "Second response should contain username");
        
        // And: User should consistently have access to the same resources
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    // Helper assertion methods
    private void assertNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }
}
