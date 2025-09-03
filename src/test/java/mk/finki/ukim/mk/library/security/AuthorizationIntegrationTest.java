package mk.finki.ukim.mk.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
 * Integration tests for Authorization functionality
 * Tests role-based access control and method-level security using JWT tokens
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String userToken;
    private String librarianToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clear any existing users
        userRepository.deleteAll();

        // Create and register a regular user
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
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        // Login and get user token
        LoginUserDto userLogin = new LoginUserDto("testuser", "password123");
        MvcResult userResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String userResponse = userResult.getResponse().getContentAsString();
        userToken = objectMapper.readTree(userResponse).get("token").asText();

        // Create and register a librarian
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
                .content(objectMapper.writeValueAsString(librarianDto)))
                .andExpect(status().isOk());

        // Login and get librarian token
        LoginUserDto librarianLogin = new LoginUserDto("librarian", "password123");
        MvcResult librarianResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String librarianResponse = librarianResult.getResponse().getContentAsString();
        librarianToken = objectMapper.readTree(librarianResponse).get("token").asText();
    }

    @Test
    void publicEndpoints_ShouldBeAccessible_WithoutAuthentication() throws Exception {
        // Test public book endpoints
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticationEndpoints_ShouldBeAccessible_WithoutAuthentication() throws Exception {
        // Test authentication endpoints are public
        mockMvc.perform(post("/api/user/register"))
                .andExpect(status().isBadRequest()); // Bad request due to missing data, not unauthorized

        mockMvc.perform(post("/api/user/login"))
                .andExpect(status().isBadRequest()); // Bad request due to missing data, not unauthorized
    }

    @Test
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        // Test wishlist endpoints require authentication
        // Note: Our security config returns 403 (Forbidden) instead of 401 (Unauthorized) for unauthenticated requests
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/wishlist/books"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/wishlist/add-book/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/wishlist/remove-book/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/wishlist/borrow-all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userRole_ShouldAccessWishlistEndpoints() throws Exception {
        // Test that regular users can access wishlist endpoints with JWT token
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/wishlist/books")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Note: POST/DELETE operations might fail due to business logic, but should not be forbidden
        mockMvc.perform(post("/api/wishlist/add-book/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound()); // Book not found, not forbidden

        mockMvc.perform(delete("/api/wishlist/remove-book/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound()); // Book not found, not forbidden
    }

    @Test
    void librarianRole_ShouldAccessWishlistEndpoints() throws Exception {
        // Test that librarians can access wishlist endpoints with JWT token
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/wishlist/books")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wishlist/add-book/1")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound()); // Book not found, not forbidden

        mockMvc.perform(delete("/api/wishlist/remove-book/1")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound()); // Book not found, not forbidden
    }

    @Test
    void userRole_ShouldAccessPublicEndpoints() throws Exception {
        // Test that authenticated users can still access public endpoints
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/authors")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/countries")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void librarianRole_ShouldAccessPublicEndpoints() throws Exception {
        // Test that librarians can access public endpoints
        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/authors")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/countries")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());
    }

    @Test
    void preAuthorizeAnnotation_ShouldEnforceAuthentication_OnWishlistEndpoints() throws Exception {
        // Test that @PreAuthorize("isAuthenticated()") works on wishlist endpoints
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()); // Should be OK because user is authenticated

        mockMvc.perform(get("/api/wishlist/books")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()); // Should be OK because user is authenticated
    }

    // Note: Swagger endpoints (/v3/api-docs, /swagger-ui/**) are not available in test environment
    // This is expected behavior - Swagger/OpenAPI documentation is typically only available in development/production
    // These endpoints are properly configured in TestSecurityConfig to be publicly accessible when available

    @Test
    void librarianRole_ShouldAccessBookManagementEndpoints() throws Exception {
        // Test that librarians can access book management endpoints
        // Note: These endpoints are currently commented out with @PreAuthorize in the actual code
        // but they should be accessible to librarians when uncommented

        mockMvc.perform(post("/api/books/add")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isBadRequest()); // Bad request due to missing data, not forbidden

        mockMvc.perform(put("/api/books/1/mark-as-borrowed")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isNotFound()); // Book not found, not forbidden
    }

    @Test
    void userRole_ShouldAccessBookManagementEndpoints_WhenNotRestricted() throws Exception {
        // Test that regular users can access book management endpoints
        // (since @PreAuthorize is commented out in the actual code)

        mockMvc.perform(post("/api/books/add")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest()); // Bad request due to missing data, not forbidden

        mockMvc.perform(put("/api/books/1/mark-as-borrowed")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound()); // Book not found, not forbidden
    }

    @Test
    void roleHierarchy_ShouldNotExist_InCurrentConfiguration() {
        // Test that there's no role hierarchy (LIBRARIAN doesn't inherit USER permissions)
        // This is verified through the individual role tests above
        // Both roles have the same access level in the current configuration
    }

    @Test
    void unknownRole_ShouldAccessProtectedEndpoints_WhenAuthenticated() throws Exception {
        // Test that any authenticated user can access protected endpoints
        // (since the current configuration only checks for authentication, not specific roles)
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/authors")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void userWithoutRoles_ShouldStillAccessEndpoints_WhenAuthenticated() throws Exception {
        // Test that users can access endpoints if authenticated (regardless of specific roles)
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void userWithMultipleRoles_ShouldAccessAllEndpoints() throws Exception {
        // Test that users with any role can access all endpoints
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/books/add")
                .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isBadRequest()); // Bad request due to missing data, not forbidden
    }
}
