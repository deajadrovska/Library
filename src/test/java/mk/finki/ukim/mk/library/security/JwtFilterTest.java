package mk.finki.ukim.mk.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.service.domain.UserService;
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtFilter class
 * Tests JWT authentication filter functionality
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class JwtFilterTest {

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtFilter jwtFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "Test", "User", Role.ROLE_USER);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenNoAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader(JwtConstants.HEADER)).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtHelper, never()).extractUsername(anyString());
        verify(userService, never()).findByUsername(anyString());
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader(JwtConstants.HEADER)).thenReturn("Basic sometoken");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtHelper, never()).extractUsername(anyString());
        verify(userService, never()).findByUsername(anyString());
    }

    @Test
    void doFilterInternal_ShouldAuthenticateUser_WhenValidJwtToken() throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        String authHeader = JwtConstants.TOKEN_PREFIX + token;
        
        when(request.getHeader(JwtConstants.HEADER)).thenReturn(authHeader);
        when(jwtHelper.extractUsername(token)).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(jwtHelper.isValid(token, testUser)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtHelper).extractUsername(token);
        verify(userService).findByUsername("testuser");
        verify(jwtHelper).isValid(token, testUser);
        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenIsInvalid() throws ServletException, IOException {
        // Given
        String token = "invalid-jwt-token";
        String authHeader = JwtConstants.TOKEN_PREFIX + token;
        
        when(request.getHeader(JwtConstants.HEADER)).thenReturn(authHeader);
        when(jwtHelper.extractUsername(token)).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(jwtHelper.isValid(token, testUser)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtHelper).extractUsername(token);
        verify(userService).findByUsername("testuser");
        verify(jwtHelper).isValid(token, testUser);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenUsernameIsNull() throws ServletException, IOException {
        // Given
        String token = "jwt-token";
        String authHeader = JwtConstants.TOKEN_PREFIX + token;
        
        when(request.getHeader(JwtConstants.HEADER)).thenReturn(authHeader);
        when(jwtHelper.extractUsername(token)).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtHelper).extractUsername(token);
        verify(userService, never()).findByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldContinueFilterChain_WhenUserAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        String token = "jwt-token";
        String authHeader = JwtConstants.TOKEN_PREFIX + token;
        Authentication existingAuth = mock(Authentication.class);
        
        when(request.getHeader(JwtConstants.HEADER)).thenReturn(authHeader);
        when(jwtHelper.extractUsername(token)).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(existingAuth);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtHelper).extractUsername(token);
        verify(userService, never()).findByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldHandleJwtException_WhenTokenIsMalformed() throws ServletException, IOException {
        // Given
        String token = "malformed-jwt-token";
        String authHeader = JwtConstants.TOKEN_PREFIX + token;
        
        when(request.getHeader(JwtConstants.HEADER)).thenReturn(authHeader);
        when(jwtHelper.extractUsername(token)).thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtHelper).extractUsername(token);
        verify(userService, never()).findByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSetAuthenticationDetails_WhenValidToken() throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        String authHeader = JwtConstants.TOKEN_PREFIX + token;
        
        when(request.getHeader(JwtConstants.HEADER)).thenReturn(authHeader);
        when(jwtHelper.extractUsername(token)).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(jwtHelper.isValid(token, testUser)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext).setAuthentication(argThat(auth -> {
            assertEquals(testUser, auth.getPrincipal());
            assertNull(auth.getCredentials());
            assertEquals(testUser.getAuthorities(), auth.getAuthorities());
            return true;
        }));
    }
}
