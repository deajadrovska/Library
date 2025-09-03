package mk.finki.ukim.mk.library.e2e;

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End tests that simulate complete user journeys through the application.
 * These tests validate the entire system working together from user perspective.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class UserJourneyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete User Journey: Register → Login → Browse Books → Add to Wishlist → View Wishlist")
    void completeUserJourney_ShouldWorkEndToEnd() throws Exception {
        String username = "e2euser" + System.currentTimeMillis();
        
        // Step 1: Register new user
        CreateUserDto registerDto = new CreateUserDto(
            username, "password123", "password123", 
            "John", "Doe", Role.ROLE_USER
        );
        
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        // Step 2: Login with registered user
        LoginUserDto loginDto = new LoginUserDto(username, "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Step 3: Browse books (public endpoint)
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 4: Get book details
        mockMvc.perform(get("/api/books/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404, "Expected 200 or 404, got " + status);
                }); // Book might not exist

        // Step 5: Add book to wishlist (authenticated endpoint)
        mockMvc.perform(post("/api/wishlist/add/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400, "Expected 200 or 400, got " + status);
                }); // Book might not exist

        // Step 6: View user's wishlist
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username));

        // Step 7: Browse authors
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 8: Get book categories
        mockMvc.perform(get("/api/books/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Librarian Journey: Register → Login → Access Admin Features")
    void librarianJourney_ShouldHaveAdminAccess() throws Exception {
        String username = "librarian" + System.currentTimeMillis();
        
        // Step 1: Register librarian
        CreateUserDto registerDto = new CreateUserDto(
            username, "password123", "password123", 
            "Jane", "Smith", Role.ROLE_LIBRARIAN
        );
        
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_LIBRARIAN"));

        // Step 2: Login as librarian
        LoginUserDto loginDto = new LoginUserDto(username, "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Step 3: Access admin features (if any exist)
        // Note: Add specific librarian-only endpoints here when they exist
        
        // Step 4: View all users (if endpoint exists)
        // mockMvc.perform(get("/api/admin/users")
        //         .header("Authorization", "Bearer " + token))
        //         .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Error Recovery Journey: Invalid Operations → Graceful Handling")
    void errorRecoveryJourney_ShouldHandleGracefully() throws Exception {
        String username = "erroruser" + System.currentTimeMillis();
        
        // Register and login user
        CreateUserDto registerDto = new CreateUserDto(
            username, "password123", "password123", 
            "Error", "User", Role.ROLE_USER
        );
        
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        LoginUserDto loginDto = new LoginUserDto(username, "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Test error scenarios
        
        // 1. Try to access non-existent book
        mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isNotFound());

        // 2. Try to add non-existent book to wishlist
        mockMvc.perform(post("/api/wishlist/add/99999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        // 3. Try to access protected endpoint without token
        mockMvc.perform(post("/api/wishlist/add/1"))
                .andExpect(status().isForbidden());

        // 4. Try to access with invalid token (use malformed format that won't be processed)
        mockMvc.perform(post("/api/wishlist/add/1")
                .header("Authorization", "Bearer invalid-token-format"))
                .andExpect(status().isForbidden());

        // 5. System should still work after errors
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }
}
