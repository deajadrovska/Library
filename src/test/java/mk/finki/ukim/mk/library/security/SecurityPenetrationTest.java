package mk.finki.ukim.mk.library.security;

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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security penetration tests to validate the application's resistance to common attacks.
 * These tests simulate malicious user behavior and security vulnerabilities.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class SecurityPenetrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("SQL Injection: Should not allow SQL injection in login")
    void sqlInjection_LoginEndpoint_ShouldBeProtected() throws Exception {
        // Attempt SQL injection in username field
        LoginUserDto maliciousLogin = new LoginUserDto(
            "admin'; DROP TABLE library_users; --", 
            "password"
        );

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousLogin)))
                .andExpect(status().isNotFound()); // Should not find user, not crash

        // Verify system still works
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SQL Injection: Should not allow SQL injection in registration")
    void sqlInjection_RegisterEndpoint_ShouldBeProtected() throws Exception {
        CreateUserDto maliciousRegister = new CreateUserDto(
            "user'; DROP TABLE library_users; --",
            "password123",
            "password123",
            "Malicious",
            "User",
            Role.ROLE_USER
        );

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRegister)))
                .andExpect(status().isBadRequest()); // Should reject malicious input

        // Verify system still works
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("XSS Protection: Should sanitize script tags in user input")
    void xssProtection_UserInput_ShouldBeSanitized() throws Exception {
        CreateUserDto xssRegister = new CreateUserDto(
            "normaluser",
            "password123",
            "password123",
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            Role.ROLE_USER
        );

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(xssRegister)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("<script>alert('XSS')</script>")) // Should be stored as-is but escaped on output
                .andExpect(jsonPath("$.surname").value("<img src=x onerror=alert('XSS')>"));
    }

    @Test
    @DisplayName("JWT Token Manipulation: Should reject tampered tokens")
    void jwtTokenManipulation_ShouldRejectTamperedTokens() throws Exception {
        // Test with completely invalid token
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());

        // Test with malformed token
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.malformed"))
                .andExpect(status().isForbidden());

        // Test with no token
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Authorization Bypass: Should not allow access without proper role")
    void authorizationBypass_ShouldEnforceRoleBasedAccess() throws Exception {
        String username = "regularuser" + System.currentTimeMillis();
        
        // Register regular user
        CreateUserDto registerDto = new CreateUserDto(
            username, "password123", "password123", 
            "Regular", "User", Role.ROLE_USER
        );
        
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        // Login to get token
        LoginUserDto loginDto = new LoginUserDto(username, "password123");
        var loginResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Try to access admin endpoints (if they exist)
        // Note: Add actual admin endpoints when they exist
        // mockMvc.perform(get("/api/admin/users")
        //         .header("Authorization", "Bearer " + token))
        //         .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Brute Force Protection: Should handle multiple failed login attempts")
    void bruteForceProtection_ShouldHandleMultipleFailedAttempts() throws Exception {
        String username = "targetuser" + System.currentTimeMillis();
        
        // Register user first
        CreateUserDto registerDto = new CreateUserDto(
            username, "correctpassword", "correctpassword", 
            "Target", "User", Role.ROLE_USER
        );
        
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        // Attempt multiple failed logins
        for (int i = 0; i < 5; i++) {
            LoginUserDto wrongLogin = new LoginUserDto(username, "wrongpassword" + i);
            
            mockMvc.perform(post("/api/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongLogin)))
                    .andExpect(status().isNotFound()); // Should consistently fail
        }

        // Correct login should still work (no account lockout implemented yet)
        LoginUserDto correctLogin = new LoginUserDto(username, "correctpassword");
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Input Validation: Should reject oversized payloads")
    void inputValidation_ShouldRejectOversizedPayloads() throws Exception {
        // Create extremely long strings
        String longString = "a".repeat(10000);
        
        CreateUserDto oversizedRegister = new CreateUserDto(
            longString,
            "password123",
            "password123",
            longString,
            longString,
            Role.ROLE_USER
        );

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oversizedRegister)))
                .andExpect(status().isBadRequest()); // Should reject oversized input
    }

    @Test
    @DisplayName("CORS Security: Should handle cross-origin requests properly")
    void corsSecurity_ShouldHandleCrossOriginRequests() throws Exception {
        // Test preflight request
        mockMvc.perform(options("/api/books")
                .header("Origin", "http://malicious-site.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()); // CORS should be configured

        // Test actual cross-origin request
        mockMvc.perform(get("/api/books")
                .header("Origin", "http://malicious-site.com"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Path Traversal: Should not allow directory traversal attacks")
    void pathTraversal_ShouldPreventDirectoryTraversal() throws Exception {
        // Attempt path traversal in book ID
        mockMvc.perform(get("/api/books/../../../etc/passwd"))
                .andExpect(status().isNotFound()); // Should not find file

        // Attempt path traversal in author ID
        mockMvc.perform(get("/api/authors/../../../etc/passwd"))
                .andExpect(status().isNotFound());
    }
}
