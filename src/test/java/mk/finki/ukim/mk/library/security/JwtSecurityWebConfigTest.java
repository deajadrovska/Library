package mk.finki.ukim.mk.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;

/**
 * Integration tests for JWT Security Configuration
 * Tests security filter chain, CORS configuration, and endpoint access rules
 * Uses real JWT tokens instead of @WithMockUser to test actual JWT authentication flow
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class JwtSecurityWebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String librarianToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create and register a user, then login to get JWT token
        CreateUserDto userDto = new CreateUserDto(
                "testuser",
                "password123",
                "password123",
                "Test",
                "User",
                Role.ROLE_USER
        );

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)));

        LoginUserDto loginDto = new LoginUserDto("testuser", "password123");

        MvcResult userResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String userResponse = userResult.getResponse().getContentAsString();
        userToken = objectMapper.readTree(userResponse).get("token").asText();

        // Create and register a librarian, then login to get JWT token
        CreateUserDto librarianDto = new CreateUserDto(
                "librarian",
                "password123",
                "password123",
                "Test",
                "Librarian",
                Role.ROLE_LIBRARIAN
        );

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianDto)));

        LoginUserDto librarianLoginDto = new LoginUserDto("librarian", "password123");

        MvcResult librarianResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianLoginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String librarianResponse = librarianResult.getResponse().getContentAsString();
        librarianToken = objectMapper.readTree(librarianResponse).get("token").asText();
    }

    @Test
    void corsHeaders_ShouldBePresent_WhenCorsEnabled() throws Exception {
        // Test CORS headers are present in response
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:3001")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // Test public API endpoints
        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());

        // Note: Swagger endpoints (/swagger-ui/**, /v3/api-docs) are not available in test environment
        // This is expected behavior - they are configured to be public when available
    }

    @Test
    void authenticationEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // Test register endpoint accessibility (will fail due to missing data, but should not be forbidden)
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()); // Bad request, not forbidden

        // Test login endpoint accessibility (will fail due to missing data, but should not be forbidden)
        // Note: Our application returns 404 for invalid login attempts, not 400
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound()); // Not found, not forbidden
    }

    @Test
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        // Note: Our security config returns 403 (Forbidden) instead of 401 (Unauthorized) for unauthenticated requests
        // Test wishlist endpoints require authentication
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/wishlist/add-book/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/wishlist/remove-book/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoints_ShouldBeAccessibleWithAuthentication() throws Exception {
        // Test wishlist endpoints with real JWT authentication
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/wishlist/books")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void corsPreflightRequest_ShouldBeHandled() throws Exception {
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:3001")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void csrfProtection_ShouldBeDisabled() throws Exception {
        // CSRF should be disabled, so POST requests without CSRF token should work
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isNotFound()); // Not forbidden due to CSRF
    }

    @Test
    void sessionManagement_ShouldBeStateless() throws Exception {
        // Test that no session is created
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", nullValue()));
    }

    // Note: JwtFilter and AuthenticationProvider configuration tests removed
    // These components are tested through their individual unit tests and integration tests

    @Test
    void unauthorizedEndpoint_ShouldReturn403() throws Exception {
        // Note: Our security config returns 403 (Forbidden) instead of 401 (Unauthorized) for unauthenticated requests
        // Test accessing a protected endpoint without authentication
        mockMvc.perform(post("/api/wishlist/add-book/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidJwtToken_ShouldReturn403() throws Exception {
        // Note: Our security config returns 403 (Forbidden) for invalid tokens
        // Test with invalid JWT token
        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformedAuthorizationHeader_ShouldReturn403() throws Exception {
        // Note: Our security config returns 403 (Forbidden) for malformed headers
        // Test with malformed authorization header
        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyAuthorizationHeader_ShouldReturn403() throws Exception {
        // Note: Our security config returns 403 (Forbidden) for empty headers
        // Test with empty authorization header
        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", ""))
                .andExpect(status().isForbidden());
    }

    @Test
    void librarianUser_ShouldAccessProtectedEndpoints() throws Exception {
        // Test that librarian can access protected endpoints with real JWT token
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());
    }

    @Test
    void httpBasicAuthentication_ShouldBeDisabled() throws Exception {
        // Test that HTTP Basic authentication is disabled
        // Note: Our security config returns 403 (Forbidden) for invalid authentication methods
        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Basic dGVzdDp0ZXN0")) // base64 encoded "test:test"
                .andExpect(status().isForbidden());
    }

    @Test
    void formLogin_ShouldBeDisabled() throws Exception {
        // Test that form login is disabled - returns 403 (forbidden) since we don't have form login configured
        mockMvc.perform(post("/login")
                        .param("username", "test")
                        .param("password", "test"))
                .andExpect(status().isForbidden()); // Form login is disabled, returns forbidden
    }
}
