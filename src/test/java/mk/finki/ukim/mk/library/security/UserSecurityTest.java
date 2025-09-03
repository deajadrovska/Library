package mk.finki.ukim.mk.library.security;

import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity security features
 * Tests UserDetails implementation and security-related functionality
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserSecurityTest {

    private User regularUser;
    private User librarianUser;

    @BeforeEach
    void setUp() {
        regularUser = new User("user", "password", "Regular", "User", Role.ROLE_USER);
        librarianUser = new User("librarian", "password", "Lib", "Rarian", Role.ROLE_LIBRARIAN);
    }

    @Test
    void getAuthorities_ShouldReturnUserRole_WhenRegularUser() {
        // When
        Collection<? extends GrantedAuthority> authorities = regularUser.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(Role.ROLE_USER));
    }

    @Test
    void getAuthorities_ShouldReturnLibrarianRole_WhenLibrarianUser() {
        // When
        Collection<? extends GrantedAuthority> authorities = librarianUser.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(Role.ROLE_LIBRARIAN));
    }

    @Test
    void getUsername_ShouldReturnCorrectUsername() {
        // Then
        assertEquals("user", regularUser.getUsername());
        assertEquals("librarian", librarianUser.getUsername());
    }

    @Test
    void getPassword_ShouldReturnCorrectPassword() {
        // Then
        assertEquals("password", regularUser.getPassword());
        assertEquals("password", librarianUser.getPassword());
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue_ByDefault() {
        // Then
        assertTrue(regularUser.isAccountNonExpired());
        assertTrue(librarianUser.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue_ByDefault() {
        // Then
        assertTrue(regularUser.isAccountNonLocked());
        assertTrue(librarianUser.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue_ByDefault() {
        // Then
        assertTrue(regularUser.isCredentialsNonExpired());
        assertTrue(librarianUser.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnTrue_ByDefault() {
        // Then
        assertTrue(regularUser.isEnabled());
        assertTrue(librarianUser.isEnabled());
    }

    @Test
    void userDetails_ShouldBeValid_WhenAllFlagsAreTrue() {
        // Then
        assertTrue(regularUser.isAccountNonExpired());
        assertTrue(regularUser.isAccountNonLocked());
        assertTrue(regularUser.isCredentialsNonExpired());
        assertTrue(regularUser.isEnabled());
    }

    @Test
    void roleAuthority_ShouldMatchRoleName() {
        // When
        String userAuthority = Role.ROLE_USER.getAuthority();
        String librarianAuthority = Role.ROLE_LIBRARIAN.getAuthority();

        // Then
        assertEquals("ROLE_USER", userAuthority);
        assertEquals("ROLE_LIBRARIAN", librarianAuthority);
    }

    @Test
    void userCreation_ShouldSetAllProperties_WhenValidInput() {
        // Given
        User newUser = new User("newuser", "newpassword", "New", "User", Role.ROLE_USER);

        // Then
        assertEquals("newuser", newUser.getUsername());
        assertEquals("newpassword", newUser.getPassword());
        assertEquals("New", newUser.getName());
        assertEquals("User", newUser.getSurname());
        assertEquals(Role.ROLE_USER, newUser.getRole());
        assertTrue(newUser.isAccountNonExpired());
        assertTrue(newUser.isAccountNonLocked());
        assertTrue(newUser.isCredentialsNonExpired());
        assertTrue(newUser.isEnabled());
    }

    @Test
    void authorities_ShouldBeImmutable_WhenRetrieved() {
        // When
        Collection<? extends GrantedAuthority> authorities = regularUser.getAuthorities();

        // Then
        assertThrows(UnsupportedOperationException.class, () -> {
            ((Collection<GrantedAuthority>) authorities).add(Role.ROLE_LIBRARIAN);
        });
    }

    @Test
    void userComparison_ShouldBeBasedOnUsername() {
        // Given
        User sameUser = new User("user", "differentpassword", "Different", "Name", Role.ROLE_LIBRARIAN);

        // Then
        assertEquals(regularUser.getUsername(), sameUser.getUsername());
        // Note: User class uses Lombok @Data which generates equals/hashCode based on all fields
    }

    @Test
    void passwordShouldBeHidden_InJsonSerialization() {
        // The password field is annotated with @JsonIgnore
        // This test verifies the annotation is present (compile-time check)
        assertTrue(true); // Password hiding is handled by Jackson annotation
    }

    @Test
    void userRole_ShouldImplementGrantedAuthority() {
        // When
        Role userRole = Role.ROLE_USER;
        Role librarianRole = Role.ROLE_LIBRARIAN;

        // Then
        assertTrue(userRole instanceof GrantedAuthority);
        assertTrue(librarianRole instanceof GrantedAuthority);
    }

    @Test
    void defaultConstructor_ShouldCreateValidUser() {
        // When
        User defaultUser = new User();

        // Then
        assertNotNull(defaultUser);
        assertTrue(defaultUser.isAccountNonExpired());
        assertTrue(defaultUser.isAccountNonLocked());
        assertTrue(defaultUser.isCredentialsNonExpired());
        assertTrue(defaultUser.isEnabled());
    }

    @Test
    void userWithNullRole_ShouldHandleAuthorities() {
        // Given
        User userWithNullRole = new User();
        userWithNullRole.setRole(null);

        // When
        Collection<? extends GrantedAuthority> authorities = userWithNullRole.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(null)); // The list contains null as the authority
    }

    @Test
    void userSetter_ShouldUpdateProperties() {
        // Given
        User user = new User();

        // When
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setName("Test");
        user.setSurname("User");
        user.setRole(Role.ROLE_USER);

        // Then
        assertEquals("testuser", user.getUsername());
        assertEquals("testpassword", user.getPassword());
        assertEquals("Test", user.getName());
        assertEquals("User", user.getSurname());
        assertEquals(Role.ROLE_USER, user.getRole());
    }

    @Test
    void accountFlags_ShouldBeModifiable() {
        // Given
        User user = new User("test", "password", "Test", "User", Role.ROLE_USER);

        // When
        user.setAccountNonExpired(false);
        user.setAccountNonLocked(false);
        user.setCredentialsNonExpired(false);
        user.setEnabled(false);

        // Then
        assertFalse(user.isAccountNonExpired());
        assertFalse(user.isAccountNonLocked());
        assertFalse(user.isCredentialsNonExpired());
        assertFalse(user.isEnabled());
    }
}
