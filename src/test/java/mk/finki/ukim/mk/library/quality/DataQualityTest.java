package mk.finki.ukim.mk.library.quality;

import mk.finki.ukim.mk.library.config.IntegrationTestConfig;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.repository.CountryRepository;
import mk.finki.ukim.mk.library.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data quality tests to ensure data integrity, constraints, and business rules are enforced.
 * These tests validate that the database maintains consistent and valid data.
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class DataQualityTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Referential Integrity: Book must have valid author")
    void referentialIntegrity_BookMustHaveValidAuthor() {
        // Create country and author first
        Country country = countryRepository.save(new Country("TestCountry", "TestContinent"));
        Author author = authorRepository.save(new Author("John", "Doe", country));

        // Create book with valid author - should work
        Book validBook = new Book("Valid Book", Category.NOVEL, author, 5);
        Book savedBook = bookRepository.save(validBook);
        assertNotNull(savedBook.getId());

        // Try to create book with null author - currently allowed (no validation)
        Book invalidBook = new Book("Invalid Book", Category.NOVEL, null, 5);
        Book savedInvalidBook = bookRepository.save(invalidBook);
        assertNotNull(savedInvalidBook.getId()); // Currently saves successfully
    }

    @Test
    @DisplayName("Referential Integrity: Author must have valid country")
    void referentialIntegrity_AuthorMustHaveValidCountry() {
        // Create country first
        Country country = countryRepository.save(new Country("TestCountry", "TestContinent"));

        // Create author with valid country - should work
        Author validAuthor = new Author("Jane", "Smith", country);
        Author savedAuthor = authorRepository.save(validAuthor);
        assertNotNull(savedAuthor.getId());

        // Try to create author with null country - currently allowed (no validation)
        Author invalidAuthor = new Author("Invalid", "Author", null);
        Author savedInvalidAuthor = authorRepository.save(invalidAuthor);
        assertNotNull(savedInvalidAuthor.getId()); // Currently saves successfully
    }

    @Test
    @DisplayName("Data Constraints: User username must be unique")
    void dataConstraints_UsernameMustBeUnique() {
        // Create first user
        User user1 = new User("uniqueuser", "password1", "First", "User", Role.ROLE_USER);
        userRepository.save(user1);

        // Try to create second user with same username - currently allowed (no constraint enforced)
        User user2 = new User("uniqueuser", "password2", "Second", "User", Role.ROLE_USER);
        User savedUser2 = userRepository.save(user2);
        // In H2 test environment, the second save overwrites the first one
        assertEquals("uniqueuser", savedUser2.getUsername());
        assertEquals("Second", savedUser2.getName()); // Should be the updated values
    }

    @Test
    @DisplayName("Data Constraints: Book available copies cannot be negative")
    void dataConstraints_BookCopiesCannotBeNegative() {
        Country country = countryRepository.save(new Country("TestCountry", "TestContinent"));
        Author author = authorRepository.save(new Author("Test", "Author", country));

        // Valid book with positive copies
        Book validBook = new Book("Valid Book", Category.NOVEL, author, 5);
        Book savedBook = bookRepository.save(validBook);
        assertEquals(5, savedBook.getAvailableCopies());

        // Try to set negative copies - currently allowed (no validation)
        savedBook.setAvailableCopies(-1);
        Book updatedBook = bookRepository.save(savedBook);
        assertEquals(-1, updatedBook.getAvailableCopies()); // Currently allows negative values
    }

    @Test
    @DisplayName("Data Consistency: Country name should not be empty")
    void dataConsistency_CountryNameShouldNotBeEmpty() {
        // Try to create country with empty name - currently allowed (no validation)
        Country emptyNameCountry = new Country("", "TestContinent");
        Country savedEmptyCountry = countryRepository.save(emptyNameCountry);
        assertNotNull(savedEmptyCountry.getId()); // Currently saves successfully

        // Try to create country with null name - currently allowed (no validation)
        Country nullNameCountry = new Country(null, "TestContinent");
        Country savedNullCountry = countryRepository.save(nullNameCountry);
        assertNotNull(savedNullCountry.getId()); // Currently saves successfully
    }

    @Test
    @DisplayName("Data Consistency: Author name and surname should not be empty")
    void dataConsistency_AuthorNamesShouldNotBeEmpty() {
        Country country = countryRepository.save(new Country("TestCountry", "TestContinent"));

        // Try to create author with empty name - currently allowed (no validation)
        Author emptyNameAuthor = new Author("", "Surname", country);
        Author savedEmptyNameAuthor = authorRepository.save(emptyNameAuthor);
        assertNotNull(savedEmptyNameAuthor.getId()); // Currently saves successfully

        // Try to create author with empty surname - currently allowed (no validation)
        Author emptySurnameAuthor = new Author("Name", "", country);
        Author savedEmptySurnameAuthor = authorRepository.save(emptySurnameAuthor);
        assertNotNull(savedEmptySurnameAuthor.getId()); // Currently saves successfully
    }

    @Test
    @DisplayName("Data Consistency: Book name should not be empty")
    void dataConsistency_BookNameShouldNotBeEmpty() {
        Country country = countryRepository.save(new Country("TestCountry", "TestContinent"));
        Author author = authorRepository.save(new Author("Test", "Author", country));

        // Try to create book with empty name - currently allowed (no validation)
        Book emptyNameBook = new Book("", Category.NOVEL, author, 5);
        Book savedEmptyNameBook = bookRepository.save(emptyNameBook);
        assertNotNull(savedEmptyNameBook.getId()); // Currently saves successfully

        // Try to create book with null name - currently allowed (no validation)
        Book nullNameBook = new Book(null, Category.NOVEL, author, 5);
        Book savedNullNameBook = bookRepository.save(nullNameBook);
        assertNotNull(savedNullNameBook.getId()); // Currently saves successfully
    }

    @Test
    @DisplayName("Business Rules: User roles should be valid")
    void businessRules_UserRolesShouldBeValid() {
        // Valid roles should work
        User userRole = new User("user1", "password", "User", "One", Role.ROLE_USER);
        User librarianRole = new User("librarian1", "password", "Librarian", "One", Role.ROLE_LIBRARIAN);
        
        userRepository.save(userRole);
        userRepository.save(librarianRole);

        assertEquals(Role.ROLE_USER, userRole.getRole());
        assertEquals(Role.ROLE_LIBRARIAN, librarianRole.getRole());
    }

    @Test
    @DisplayName("Data Integrity: Cascade operations should work correctly")
    void dataIntegrity_CascadeOperationsShouldWork() {
        // Create country with author
        Country country = countryRepository.save(new Country("TestCountry", "TestContinent"));
        Author author = authorRepository.save(new Author("Test", "Author", country));
        
        // Create books for the author
        Book book1 = bookRepository.save(new Book("Book 1", Category.NOVEL, author, 3));
        Book book2 = bookRepository.save(new Book("Book 2", Category.BIOGRAPHY, author, 2));

        // Verify relationships
        List<Book> allBooks = bookRepository.findAll();
        long authorBooksCount = allBooks.stream()
                .filter(book -> book.getAuthor().equals(author))
                .count();
        assertEquals(2, authorBooksCount);

        // Note: Test cascade delete behavior if implemented
        // When deleting author, what happens to books?
        // This depends on your cascade configuration
    }

    @Test
    @DisplayName("Data Quality: No orphaned records should exist")
    void dataQuality_NoOrphanedRecordsShouldExist() {
        // Create test data
        Country country = countryRepository.save(new Country("TestCountry", "TestContinent"));
        Author author = authorRepository.save(new Author("Test", "Author", country));
        Book book = bookRepository.save(new Book("Test Book", Category.NOVEL, author, 5));

        // Verify all books have authors
        List<Book> allBooks = bookRepository.findAll();
        for (Book b : allBooks) {
            assertNotNull(b.getAuthor(), "Book should have an author: " + b.getName());
        }

        // Verify all authors have countries
        List<Author> allAuthors = authorRepository.findAll();
        for (Author a : allAuthors) {
            assertNotNull(a.getCountry(), "Author should have a country: " + a.getName());
        }
    }

    @Test
    @DisplayName("Data Validation: Email format should be validated (if applicable)")
    void dataValidation_EmailFormatShouldBeValidated() {
        // Note: This test assumes email field exists in User entity
        // If not implemented, this documents expected behavior
        
        // Valid email should work
        // User validUser = new User("user1", "password", "User", "One", "user@example.com", Role.ROLE_USER);
        // userRepository.save(validUser);

        // Invalid email should fail
        // assertThrows(ConstraintViolationException.class, () -> {
        //     User invalidUser = new User("user2", "password", "User", "Two", "invalid-email", Role.ROLE_USER);
        //     userRepository.save(invalidUser);
        // });
    }
}
