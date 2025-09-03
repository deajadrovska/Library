package mk.finki.ukim.mk.library.config;

import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserContext class
 * Tests security context management and current user retrieval
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserContextTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserContext userContext;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "Test", "User", Role.ROLE_USER);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenAuthenticatedWithUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // When
        User currentUser = userContext.getCurrentUser();

        // Then
        assertNotNull(currentUser);
        assertEquals("testuser", currentUser.getUsername());
        assertEquals("Test", currentUser.getName());
        assertEquals("User", currentUser.getSurname());
        assertEquals(Role.ROLE_USER, currentUser.getRole());
    }

    @Test
    void getCurrentUser_ShouldReturnNull_WhenNoAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        User currentUser = userContext.getCurrentUser();

        // Then
        assertNull(currentUser);
    }

    @Test
    void getCurrentUser_ShouldReturnNull_WhenPrincipalIsNotUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("string-principal");

        // When
        User currentUser = userContext.getCurrentUser();

        // Then
        assertNull(currentUser);
    }

    @Test
    void getCurrentUsername_ShouldReturnUsername_WhenAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        // When
        String currentUsername = userContext.getCurrentUsername();

        // Then
        assertEquals("testuser", currentUsername);
    }

    @Test
    void getCurrentUsername_ShouldReturnNull_WhenNoAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        String currentUsername = userContext.getCurrentUsername();

        // Then
        assertNull(currentUsername);
    }

    @Test
    void getCurrentUser_ShouldReturnLibrarian_WhenAuthenticatedAsLibrarian() {
        // Given
        User librarianUser = new User("librarian", "password", "Lib", "Rarian", Role.ROLE_LIBRARIAN);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(librarianUser);

        // When
        User currentUser = userContext.getCurrentUser();

        // Then
        assertNotNull(currentUser);
        assertEquals("librarian", currentUser.getUsername());
        assertEquals(Role.ROLE_LIBRARIAN, currentUser.getRole());
    }

    @Test
    void getCurrentUsername_ShouldReturnCorrectUsername_WhenMultipleCalls() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        // When
        String firstCall = userContext.getCurrentUsername();
        String secondCall = userContext.getCurrentUsername();

        // Then
        assertEquals("testuser", firstCall);
        assertEquals("testuser", secondCall);
        assertEquals(firstCall, secondCall);
    }

    @Test
    void getCurrentUser_ShouldHandleNullPrincipal_WhenAuthenticationExists() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        User currentUser = userContext.getCurrentUser();

        // Then
        assertNull(currentUser);
    }

    @Test
    void getCurrentUsername_ShouldHandleNullName_WhenAuthenticationExists() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(null);

        // When
        String currentUsername = userContext.getCurrentUsername();

        // Then
        assertNull(currentUsername);
    }

    @Test
    void getCurrentUser_ShouldReturnSameInstance_WhenCalledMultipleTimes() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        // When
        User firstCall = userContext.getCurrentUser();
        User secondCall = userContext.getCurrentUser();

        // Then
        assertSame(firstCall, secondCall);
    }
}
