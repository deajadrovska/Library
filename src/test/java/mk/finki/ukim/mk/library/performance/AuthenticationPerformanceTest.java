package mk.finki.ukim.mk.library.performance;

import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for Authentication and User Management endpoints.
 * These are critical for user experience as they're the entry point to the system.
 */
@DisplayName("Authentication Performance Tests")
public class AuthenticationPerformanceTest extends BasePerformanceTest {

    @Test
    @DisplayName("Load Test: POST /api/user/login - User Authentication")
    void testLoginPerformance() throws Exception {
        // Most critical endpoint - users logging in
        // This directly impacts user experience and system accessibility
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                LoginUserDto loginDto = new LoginUserDto(uniqueUsername, "password123");
                
                mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 100, 10); // 100 concurrent users, 10 login attempts each = 1000 total requests

        // Requirements: 98% success rate, under 300ms (authentication must be fast)
        assertPerformanceRequirements(result, 98.0, 300.0);
    }

    @Test
    @DisplayName("Load Test: POST /api/user/register - User Registration")
    void testRegistrationPerformance() throws Exception {
        // New user registration - less frequent but important for user onboarding
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                String uniqueUsername = "testuser" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
                CreateUserDto userDto = new CreateUserDto(
                        uniqueUsername,
                        "password123",
                        "password123",
                        "Test",
                        "User",
                        Role.ROLE_USER
                );
                
                mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                        .andExpect(isOkOrBadRequest()); // Some may fail due to constraints
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 50, 5); // 50 concurrent registrations, 5 attempts each = 250 total requests

        // Requirements: 90% success rate (some failures expected), under 500ms
        assertPerformanceRequirements(result, 90.0, 500.0);
    }

    @Test
    @DisplayName("Stress Test: Failed Login Attempts")
    void testFailedLoginPerformance() throws Exception {
        // Test system behavior under failed authentication attempts
        // Important for security and system stability
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                LoginUserDto invalidLogin = new LoginUserDto("invaliduser", "wrongpassword");
                
                mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                        .andExpect(status().isNotFound()); // Expecting 404 for invalid credentials
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 80, 15); // 80 concurrent failed attempts, 15 each = 1200 total requests

        // Requirements: 95% success rate (should handle failed attempts gracefully), under 200ms
        assertPerformanceRequirements(result, 95.0, 200.0);
    }

    @Test
    @DisplayName("Load Test: Mixed Authentication Operations")
    void testMixedAuthenticationPerformance() throws Exception {
        // Simulate real-world authentication patterns
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                double random = Math.random();
                
                if (random < 0.7) {
                    // 70% - Successful login attempts
                    LoginUserDto loginDto = new LoginUserDto(uniqueUsername, "password123");
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                            .andExpect(status().isOk());
                } else if (random < 0.85) {
                    // 15% - Failed login attempts
                    LoginUserDto invalidLogin = new LoginUserDto("invaliduser", "wrongpassword");
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidLogin)))
                            .andExpect(status().isNotFound());
                } else {
                    // 15% - Registration attempts
                    String uniqueUsername = "mixeduser" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
                    CreateUserDto userDto = new CreateUserDto(
                            uniqueUsername,
                            "password123",
                            "password123",
                            "Mixed",
                            "User",
                            Role.ROLE_USER
                    );
                    mockMvc.perform(post("/api/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDto)))
                            .andExpect(isOkOrBadRequest());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 120, 25); // 120 concurrent users, 25 mixed operations each = 3000 total requests

        // Requirements: 92% success rate, under 350ms average
        assertPerformanceRequirements(result, 92.0, 350.0);
    }

    @Test
    @DisplayName("Burst Test: High-Frequency Login Attempts")
    void testLoginBurstPerformance() throws Exception {
        // Test system behavior under sudden high load (e.g., start of business day)
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                LoginUserDto loginDto = new LoginUserDto(uniqueUsername, "password123");
                
                mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 200, 5); // 200 concurrent users, 5 quick login attempts each = 1000 total requests

        // Requirements: 96% success rate, under 400ms (system should handle bursts)
        assertPerformanceRequirements(result, 96.0, 400.0);
    }

    @Test
    @DisplayName("Endurance Test: Sustained Authentication Load")
    void testSustainedAuthenticationLoad() throws Exception {
        // Test system stability under sustained authentication load
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                // Alternate between user and librarian logins
                boolean useLibrarian = Math.random() < 0.2; // 20% librarian, 80% user
                
                if (useLibrarian) {
                    LoginUserDto librarianLogin = new LoginUserDto(uniqueLibrarianUsername, "password123");
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(librarianLogin)))
                            .andExpect(status().isOk());
                } else {
                    LoginUserDto userLogin = new LoginUserDto(uniqueUsername, "password123");
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLogin)))
                            .andExpect(status().isOk());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 75, 40); // 75 concurrent users, 40 sustained requests each = 3000 total requests

        // Requirements: 97% success rate, under 300ms (should maintain performance over time)
        assertPerformanceRequirements(result, 97.0, 300.0);
    }

    @Test
    @DisplayName("Security Test: Malformed Request Handling")
    void testMalformedRequestPerformance() throws Exception {
        // Test system resilience against malformed authentication requests
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                double random = Math.random();
                
                if (random < 0.33) {
                    // Empty JSON
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                            .andExpect(status().isNotFound());
                } else if (random < 0.66) {
                    // Invalid JSON structure
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"invalid\": \"structure\"}"))
                            .andExpect(status().isNotFound());
                } else {
                    // Malformed JSON
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json"))
                            .andExpect(status().isBadRequest());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 60, 20); // 60 concurrent malformed requests, 20 each = 1200 total requests

        // Requirements: 95% success rate (should handle malformed requests gracefully), under 150ms
        assertPerformanceRequirements(result, 95.0, 150.0);
    }
}
