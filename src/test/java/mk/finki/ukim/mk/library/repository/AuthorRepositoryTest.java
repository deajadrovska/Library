package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.model.projections.AuthorNameProjection;
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
class AuthorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Country testCountry1;
    private Country testCountry2;
    private Author testAuthor1;
    private Author testAuthor2;
    private Author testAuthor3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        authorRepository.deleteAll();
        countryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test countries
        testCountry1 = new Country("United States", "North America");
        testCountry2 = new Country("United Kingdom", "Europe");

        entityManager.persistAndFlush(testCountry1);
        entityManager.persistAndFlush(testCountry2);

        // Create test authors
        testAuthor1 = new Author("Stephen", "King", testCountry1);
        testAuthor2 = new Author("J.K.", "Rowling", testCountry2);
        testAuthor3 = new Author("George", "Martin", testCountry1);

        entityManager.persistAndFlush(testAuthor1);
        entityManager.persistAndFlush(testAuthor2);
        entityManager.persistAndFlush(testAuthor3);
        entityManager.clear();
    }

    @Test
    void findAll_ShouldReturnAllAuthors() {
        // When
        List<Author> result = authorRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Author::getName)
                .containsExactlyInAnyOrder("Stephen", "J.K.", "George");
        assertThat(result).extracting(Author::getSurname)
                .containsExactlyInAnyOrder("King", "Rowling", "Martin");
    }

    @Test
    void findAllProjectedBy_ShouldReturnAuthorNameProjections() {
        // When
        List<AuthorNameProjection> result = authorRepository.findAllProjectedBy();

        // Then
        assertThat(result).hasSize(3);
        
        // Verify projection contains only name and surname (not full entity)
        result.forEach(projection -> {
            assertThat(projection.getName()).isNotNull();
            assertThat(projection.getSurname()).isNotNull();
        });

        // Verify specific authors are present
        assertThat(result).extracting(AuthorNameProjection::getName)
                .containsExactlyInAnyOrder("Stephen", "J.K.", "George");
        assertThat(result).extracting(AuthorNameProjection::getSurname)
                .containsExactlyInAnyOrder("King", "Rowling", "Martin");
    }

    @Test
    void findById_ShouldReturnAuthor_WhenAuthorExists() {
        // Given
        Long authorId = testAuthor1.getId();

        // When
        Optional<Author> result = authorRepository.findById(authorId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Stephen");
        assertThat(result.get().getSurname()).isEqualTo("King");
        assertThat(result.get().getCountry().getName()).isEqualTo("United States");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenAuthorNotFound() {
        // When
        Optional<Author> result = authorRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistAuthor_WhenValidAuthorProvided() {
        // Given
        Author newAuthor = new Author("Agatha", "Christie", testCountry2);

        // When
        Author savedAuthor = authorRepository.save(newAuthor);

        // Then
        assertThat(savedAuthor).isNotNull();
        assertThat(savedAuthor.getId()).isNotNull();
        assertThat(savedAuthor.getName()).isEqualTo("Agatha");
        assertThat(savedAuthor.getSurname()).isEqualTo("Christie");
        assertThat(savedAuthor.getCountry().getName()).isEqualTo("United Kingdom");

        // Verify persistence
        Optional<Author> foundAuthor = authorRepository.findById(savedAuthor.getId());
        assertThat(foundAuthor).isPresent();
        assertThat(foundAuthor.get().getName()).isEqualTo("Agatha");
    }

    @Test
    void save_ShouldUpdateAuthor_WhenExistingAuthorModified() {
        // Given
        testAuthor1.setName("Stephen Edwin");
        testAuthor1.setSurname("King Jr.");

        // When
        Author updatedAuthor = authorRepository.save(testAuthor1);

        // Then
        assertThat(updatedAuthor.getName()).isEqualTo("Stephen Edwin");
        assertThat(updatedAuthor.getSurname()).isEqualTo("King Jr.");

        // Verify persistence
        Optional<Author> foundAuthor = authorRepository.findById(testAuthor1.getId());
        assertThat(foundAuthor).isPresent();
        assertThat(foundAuthor.get().getName()).isEqualTo("Stephen Edwin");
        assertThat(foundAuthor.get().getSurname()).isEqualTo("King Jr.");
    }

    @Test
    void delete_ShouldRemoveAuthor_WhenAuthorExists() {
        // Given
        Long authorId = testAuthor1.getId();
        assertThat(authorRepository.findById(authorId)).isPresent();

        // When
        authorRepository.deleteById(authorId);
        entityManager.flush();

        // Then
        assertThat(authorRepository.findById(authorId)).isEmpty();
        assertThat(authorRepository.findAll()).hasSize(2);
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When & Then
        assertThat(authorRepository.count()).isEqualTo(3);
    }

    @Test
    void existsById_ShouldReturnCorrectStatus() {
        // Given
        Long existingId = testAuthor1.getId();

        // When & Then
        assertThat(authorRepository.existsById(existingId)).isTrue();
        assertThat(authorRepository.existsById(999L)).isFalse();
    }

    @Test
    void findAllProjectedBy_ShouldReturnEmptyList_WhenNoAuthorsExist() {
        // Given
        authorRepository.deleteAll();
        entityManager.flush();

        // When
        List<AuthorNameProjection> result = authorRepository.findAllProjectedBy();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void authorCountryRelationship_ShouldBeProperlyMaintained() {
        // When
        List<Author> usAuthors = authorRepository.findAll().stream()
                .filter(author -> "United States".equals(author.getCountry().getName()))
                .toList();

        List<Author> ukAuthors = authorRepository.findAll().stream()
                .filter(author -> "United Kingdom".equals(author.getCountry().getName()))
                .toList();

        // Then
        assertThat(usAuthors).hasSize(2);
        assertThat(usAuthors).extracting(Author::getName)
                .containsExactlyInAnyOrder("Stephen", "George");

        assertThat(ukAuthors).hasSize(1);
        assertThat(ukAuthors.get(0).getName()).isEqualTo("J.K.");
    }
}
