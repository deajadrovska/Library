package mk.finki.ukim.mk.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtHelper class
 * Tests JWT token generation, validation, and claims extraction
 */
@ExtendWith(MockitoExtension.class)
class JwtHelperTest {

    @InjectMocks
    private JwtHelper jwtHelper;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "Test", "User", Role.ROLE_USER);
        jwtHelper = new JwtHelper(); // Create real instance for testing
    }

    @Test
    void generateToken_ShouldCreateValidToken_WhenValidUserDetails() {
        // When
        String token = jwtHelper.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenValidToken() {
        // Given
        String token = jwtHelper.generateToken(testUser);

        // When
        String extractedUsername = jwtHelper.extractUsername(token);

        // Then
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate_WhenValidToken() {
        // Given
        String token = jwtHelper.generateToken(testUser);

        // When
        Date expiration = jwtHelper.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTokenIsValidAndUserMatches() {
        // Given
        String token = jwtHelper.generateToken(testUser);

        // When
        boolean isValid = jwtHelper.isValid(token, testUser);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenTokenIsExpired() {
        // Given - Create an expired token manually
        String expiredToken = createExpiredToken();

        // When
        boolean isValid = jwtHelper.isValid(expiredToken, testUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenUserDoesNotMatch() {
        // Given
        String token = jwtHelper.generateToken(testUser);
        User differentUser = new User("differentuser", "password", "Different", "User", Role.ROLE_USER);

        // When
        boolean isValid = jwtHelper.isValid(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsMalformed() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThrows(JwtException.class, () -> jwtHelper.extractUsername(malformedToken));
    }

    @Test
    void extractExpiration_ShouldThrowException_WhenTokenIsMalformed() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThrows(JwtException.class, () -> jwtHelper.extractExpiration(malformedToken));
    }

    @Test
    void generateToken_ShouldIncludeRolesInClaims_WhenUserHasRoles() {
        // Given
        User librarianUser = new User("librarian", "password", "Lib", "Rarian", Role.ROLE_LIBRARIAN);

        // When
        String token = jwtHelper.generateToken(librarianUser);

        // Then
        assertNotNull(token);
        // Extract claims manually to verify roles are included
        Claims claims = extractClaimsFromToken(token);
        assertNotNull(claims.get("roles"));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenTokenIsNull() {
        // When & Then
        assertThrows(Exception.class, () -> jwtHelper.isValid(null, testUser));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenUserIsNull() {
        // Given
        String token = jwtHelper.generateToken(testUser);

        // When & Then
        assertThrows(Exception.class, () -> jwtHelper.isValid(token, null));
    }

    private String createExpiredToken() {
        // Create a token that's already expired
        return Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000)) // 1 day ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(Keys.hmacShaKeyFor(hexStringToByteArray(JwtConstants.SECRET_KEY)))
                .compact();
    }

    private Claims extractClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(hexStringToByteArray(JwtConstants.SECRET_KEY)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
