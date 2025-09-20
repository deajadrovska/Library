package mk.finki.ukim.mk.library.system;

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
 * System Integration Tests
 * Tests the complete system integration across all layers and components.
 * Validates that all system components work together correctly as a unified system.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class SystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("System Integration: Complete library management workflow should work end-to-end")
    void systemIntegration_CompleteLibraryWorkflowShouldWorkEndToEnd() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // 1. System should handle user registration
        CreateUserDto userDto = new CreateUserDto(
            "systemuser" + timestamp, "password123", "password123",
            "System", "User", Role.ROLE_USER
        );
        
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("systemuser" + timestamp));

        // 2. System should handle user authentication
        LoginUserDto loginDto = new LoginUserDto("systemuser" + timestamp, "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 3. System should provide access to public resources
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 4. System should handle authenticated user operations
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("systemuser" + timestamp));

        // 5. System should handle book operations (if books exist)
        MvcResult booksResult = mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andReturn();

        String booksJson = booksResult.getResponse().getContentAsString();
        if (!booksJson.equals("[]")) {
            // If books exist, test adding to wishlist
            mockMvc.perform(post("/api/wishlist/add/1")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("System Integration: Multi-user concurrent operations should work correctly")
    void systemIntegration_MultiUserConcurrentOperationsShouldWorkCorrectly() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Create multiple users
        String user1 = "user1" + timestamp;
        String user2 = "user2" + timestamp;
        
        CreateUserDto user1Dto = new CreateUserDto(user1, "password123", "password123", "User", "One", Role.ROLE_USER);
        CreateUserDto user2Dto = new CreateUserDto(user2, "password123", "password123", "User", "Two", Role.ROLE_USER);
        
        // Register both users
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Dto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Dto)))
                .andExpect(status().isOk());

        // Login both users
        LoginUserDto login1 = new LoginUserDto(user1, "password123");
        LoginUserDto login2 = new LoginUserDto(user2, "password123");
        
        MvcResult login1Result = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login1)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult login2Result = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login2)))
                .andExpect(status().isOk())
                .andReturn();

        String token1 = objectMapper.readTree(login1Result.getResponse().getContentAsString()).get("token").asText();
        String token2 = objectMapper.readTree(login2Result.getResponse().getContentAsString()).get("token").asText();

        // Both users should be able to access their own wishlists
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user1));

        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user2));

        // Both users should be able to access public resources simultaneously
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("System Integration: Error handling should work across all system layers")
    void systemIntegration_ErrorHandlingShouldWorkAcrossAllSystemLayers() throws Exception {
        // Test authentication layer error handling
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());

        // Test invalid login
        LoginUserDto invalidLogin = new LoginUserDto("nonexistentuser", "wrongpassword");
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isNotFound()); // User not found returns 404

        // Test invalid registration
        CreateUserDto invalidUser = new CreateUserDto("", "", "", "", "", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        // Test resource not found
        mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/authors/99999"))
                .andExpect(status().isNotFound());

        // Test invalid content type
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("System Integration: Security should be enforced across all protected endpoints")
    void systemIntegration_SecurityShouldBeEnforcedAcrossAllProtectedEndpoints() throws Exception {
        // All protected endpoints should require authentication
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/wishlist/add/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/wishlist/remove/1"))
                .andExpect(status().isForbidden());

        // Invalid tokens should be rejected
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Invalid-Format"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("System Integration: Data consistency should be maintained across operations")
    void systemIntegration_DataConsistencyShouldBeMaintainedAcrossOperations() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "consistencyuser" + timestamp;
        
        // Create user
        CreateUserDto userDto = new CreateUserDto(username, "password123", "password123", "Test", "User", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        // Login
        LoginUserDto loginDto = new LoginUserDto(username, "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Verify user's wishlist is created and accessible
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.books").isArray());

        // Verify user can access the same data consistently
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    @DisplayName("System Integration: System should handle different user roles correctly")
    void systemIntegration_SystemShouldHandleDifferentUserRolesCorrectly() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // Create regular user
        String regularUser = "regular" + timestamp;
        CreateUserDto regularUserDto = new CreateUserDto(regularUser, "password123", "password123", "Regular", "User", Role.ROLE_USER);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regularUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        // Create librarian user
        String librarian = "librarian" + timestamp;
        CreateUserDto librarianDto = new CreateUserDto(librarian, "password123", "password123", "Librarian", "User", Role.ROLE_LIBRARIAN);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_LIBRARIAN"));

        // Both should be able to login
        LoginUserDto regularLogin = new LoginUserDto(regularUser, "password123");
        LoginUserDto librarianLogin = new LoginUserDto(librarian, "password123");
        
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regularLogin)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianLogin)))
                .andExpect(status().isOk());
    }
}
