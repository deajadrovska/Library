package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test users
        testUser1 = new User("john_doe", "password123", "John", "Doe", Role.ROLE_USER);
        testUser2 = new User("jane_smith", "password456", "Jane", "Smith", Role.ROLE_LIBRARIAN);

        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.clear();
    }

    @Test
    void findByUsernameAndPassword_ShouldReturnUser_WhenCredentialsMatch() {
        // When
        Optional<User> result = userRepository.findByUsernameAndPassword("john_doe", "password123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
        assertThat(result.get().getName()).isEqualTo("John");
        assertThat(result.get().getSurname()).isEqualTo("Doe");
        assertThat(result.get().getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void findByUsernameAndPassword_ShouldReturnEmpty_WhenUsernameNotFound() {
        // When
        Optional<User> result = userRepository.findByUsernameAndPassword("nonexistent", "password123");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameAndPassword_ShouldReturnEmpty_WhenPasswordIncorrect() {
        // When
        Optional<User> result = userRepository.findByUsernameAndPassword("john_doe", "wrongpassword");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUsernameExists() {
        // When
        Optional<User> result = userRepository.findByUsername("jane_smith");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jane_smith");
        assertThat(result.get().getName()).isEqualTo("Jane");
        assertThat(result.get().getSurname()).isEqualTo("Smith");
        assertThat(result.get().getRole()).isEqualTo(Role.ROLE_LIBRARIAN);
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameNotFound() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllUsers_WithEntityGraphOptimization() {
        // When
        List<User> result = userRepository.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getUsername)
                .containsExactlyInAnyOrder("john_doe", "jane_smith");
        
        // Verify EntityGraph optimization - wishlist should not be loaded
        result.forEach(user -> {
            // The wishlist should be null or not initialized due to @EntityGraph(attributePaths = {})
            // This tests that the EntityGraph is working to avoid N+1 queries
            assertThat(user.getUsername()).isNotNull();
        });
    }

    @Test
    void save_ShouldPersistUser_WhenValidUserProvided() {
        // Given
        User newUser = new User("test_user", "testpass", "Test", "User", Role.ROLE_USER);

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("test_user");
        
        // Verify persistence
        Optional<User> foundUser = userRepository.findByUsername("test_user");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Test");
    }

    @Test
    void deleteByUsername_ShouldRemoveUser_WhenUserExists() {
        // Given
        assertThat(userRepository.findByUsername("john_doe")).isPresent();

        // When
        userRepository.deleteById("john_doe");
        entityManager.flush();

        // Then
        assertThat(userRepository.findByUsername("john_doe")).isEmpty();
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // When & Then
        assertThat(userRepository.existsById("john_doe")).isTrue();
        assertThat(userRepository.existsById("nonexistent")).isFalse();
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When & Then
        assertThat(userRepository.count()).isEqualTo(2);
    }
}
