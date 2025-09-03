package mk.finki.ukim.mk.library.config;

import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.service.domain.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CustomUsernamePasswordAuthenticationProvider
 * Tests authentication logic and credential validation
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CustomUsernamePasswordAuthenticationProviderTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUsernamePasswordAuthenticationProvider authenticationProvider;

    private User testUser;
    private UsernamePasswordAuthenticationToken authenticationToken;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encodedPassword", "Test", "User", Role.ROLE_USER);
        authenticationToken = new UsernamePasswordAuthenticationToken("testuser", "rawPassword");
    }

    @Test
    void authenticate_ShouldReturnAuthentication_WhenValidCredentials() {
        // Given
        when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // When
        Authentication result = authenticationProvider.authenticate(authenticationToken);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getPrincipal());
        assertEquals("encodedPassword", result.getCredentials());
        assertEquals(testUser.getAuthorities(), result.getAuthorities());
        assertTrue(result.isAuthenticated());
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenEmptyUsername() {
        // Given
        UsernamePasswordAuthenticationToken emptyUsernameToken = 
            new UsernamePasswordAuthenticationToken("", "password");

        // When & Then
        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> authenticationProvider.authenticate(emptyUsernameToken)
        );
        assertEquals("Invalid Credentials", exception.getMessage());
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenEmptyPassword() {
        // Given
        UsernamePasswordAuthenticationToken emptyPasswordToken = 
            new UsernamePasswordAuthenticationToken("testuser", "");

        // When & Then
        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> authenticationProvider.authenticate(emptyPasswordToken)
        );
        assertEquals("Invalid Credentials", exception.getMessage());
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenPasswordDoesNotMatch() {
        // Given
        when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        UsernamePasswordAuthenticationToken wrongPasswordToken = 
            new UsernamePasswordAuthenticationToken("testuser", "wrongPassword");

        // When & Then
        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> authenticationProvider.authenticate(wrongPasswordToken)
        );
        assertEquals("Password is incorrect!", exception.getMessage());
    }

    @Test
    void authenticate_ShouldCallUserService_WhenValidUsername() {
        // Given
        when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When
        authenticationProvider.authenticate(authenticationToken);

        // Then
        verify(userService).loadUserByUsername("testuser");
    }

    @Test
    void authenticate_ShouldCallPasswordEncoder_WhenValidCredentials() {
        // Given
        when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // When
        authenticationProvider.authenticate(authenticationToken);

        // Then
        verify(passwordEncoder).matches("rawPassword", "encodedPassword");
    }

    @Test
    void supports_ShouldReturnTrue_WhenUsernamePasswordAuthenticationToken() {
        // When
        boolean supports = authenticationProvider.supports(UsernamePasswordAuthenticationToken.class);

        // Then
        assertTrue(supports);
    }

    @Test
    void supports_ShouldReturnFalse_WhenOtherAuthenticationClass() {
        // When
        boolean supports = authenticationProvider.supports(Authentication.class);

        // Then
        assertFalse(supports);
    }

    @Test
    void authenticate_ShouldPreserveUserAuthorities_WhenSuccessful() {
        // Given
        User librarianUser = new User("librarian", "encodedPassword", "Lib", "Rarian", Role.ROLE_LIBRARIAN);
        UsernamePasswordAuthenticationToken librarianToken = 
            new UsernamePasswordAuthenticationToken("librarian", "rawPassword");
        
        when(userService.loadUserByUsername("librarian")).thenReturn(librarianUser);
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // When
        Authentication result = authenticationProvider.authenticate(librarianToken);

        // Then
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(Role.ROLE_LIBRARIAN));
    }


    @Test
    void authenticate_ShouldHandleNullUsername_WhenUsernameIsNull() {
        // Given
        UsernamePasswordAuthenticationToken nullUsernameToken = 
            new UsernamePasswordAuthenticationToken(null, "password");

        // When & Then
        assertThrows(
            BadCredentialsException.class,
            () -> authenticationProvider.authenticate(nullUsernameToken)
        );
    }
}
