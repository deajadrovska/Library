package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.model.domain.Category;
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
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Author testAuthor1;
    private Author testAuthor2;
    private Book testBook1;
    private Book testBook2;
    private Book testBook3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        countryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test countries
        Country country1 = new Country("United States", "North America");
        Country country2 = new Country("United Kingdom", "Europe");
        entityManager.persistAndFlush(country1);
        entityManager.persistAndFlush(country2);

        // Create test authors
        testAuthor1 = new Author("Stephen", "King", country1);
        testAuthor2 = new Author("J.K.", "Rowling", country2);
        entityManager.persistAndFlush(testAuthor1);
        entityManager.persistAndFlush(testAuthor2);

        // Create test books
        testBook1 = new Book("The Shining", Category.NOVEL, testAuthor1, 5);
        testBook2 = new Book("Harry Potter", Category.FANTASY, testAuthor2, 10);
        testBook3 = new Book("IT", Category.NOVEL, testAuthor1, 3);

        entityManager.persistAndFlush(testBook1);
        entityManager.persistAndFlush(testBook2);
        entityManager.persistAndFlush(testBook3);
        entityManager.clear();
    }

    @Test
    void findAll_ShouldReturnAllBooks() {
        // When
        List<Book> result = bookRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Book::getName)
                .containsExactlyInAnyOrder("The Shining", "Harry Potter", "IT");
        assertThat(result).extracting(Book::getCategory)
                .containsExactlyInAnyOrder(Category.NOVEL, Category.FANTASY, Category.NOVEL);
    }

    @Test
    void findById_ShouldReturnBook_WhenBookExists() {
        // Given
        Long bookId = testBook1.getId();

        // When
        Optional<Book> result = bookRepository.findById(bookId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("The Shining");
        assertThat(result.get().getCategory()).isEqualTo(Category.NOVEL);
        assertThat(result.get().getAuthor().getName()).isEqualTo("Stephen");
        assertThat(result.get().getAvailableCopies()).isEqualTo(5);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenBookNotFound() {
        // When
        Optional<Book> result = bookRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistBook_WhenValidBookProvided() {
        // Given
        Book newBook = new Book("Pet Sematary", Category.THRILER, testAuthor1, 7);

        // When
        Book savedBook = bookRepository.save(newBook);

        // Then
        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getName()).isEqualTo("Pet Sematary");
        assertThat(savedBook.getCategory()).isEqualTo(Category.THRILER);
        assertThat(savedBook.getAuthor().getName()).isEqualTo("Stephen");
        assertThat(savedBook.getAvailableCopies()).isEqualTo(7);

        // Verify persistence
        Optional<Book> foundBook = bookRepository.findById(savedBook.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getName()).isEqualTo("Pet Sematary");
    }

    @Test
    void save_ShouldUpdateBook_WhenExistingBookModified() {
        // Given
        testBook1.setName("The Shining - Updated");
        testBook1.setAvailableCopies(8);
        testBook1.setCategory(Category.THRILER);

        // When
        Book updatedBook = bookRepository.save(testBook1);

        // Then
        assertThat(updatedBook.getName()).isEqualTo("The Shining - Updated");
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(8);
        assertThat(updatedBook.getCategory()).isEqualTo(Category.THRILER);

        // Verify persistence
        Optional<Book> foundBook = bookRepository.findById(testBook1.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getName()).isEqualTo("The Shining - Updated");
        assertThat(foundBook.get().getAvailableCopies()).isEqualTo(8);
        assertThat(foundBook.get().getCategory()).isEqualTo(Category.THRILER);
    }

    @Test
    void delete_ShouldRemoveBook_WhenBookExists() {
        // Given
        Long bookId = testBook1.getId();
        assertThat(bookRepository.findById(bookId)).isPresent();

        // When
        bookRepository.deleteById(bookId);
        entityManager.flush();

        // Then
        assertThat(bookRepository.findById(bookId)).isEmpty();
        assertThat(bookRepository.findAll()).hasSize(2);
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When & Then
        assertThat(bookRepository.count()).isEqualTo(3);
    }

    @Test
    void existsById_ShouldReturnCorrectStatus() {
        // Given
        Long existingId = testBook1.getId();

        // When & Then
        assertThat(bookRepository.existsById(existingId)).isTrue();
        assertThat(bookRepository.existsById(999L)).isFalse();
    }

    @Test
    void bookAuthorRelationship_ShouldBeProperlyMaintained() {
        // When
        List<Book> stephenKingBooks = bookRepository.findAll().stream()
                .filter(book -> "Stephen".equals(book.getAuthor().getName()))
                .toList();

        List<Book> jkRowlingBooks = bookRepository.findAll().stream()
                .filter(book -> "J.K.".equals(book.getAuthor().getName()))
                .toList();

        // Then
        assertThat(stephenKingBooks).hasSize(2);
        assertThat(stephenKingBooks).extracting(Book::getName)
                .containsExactlyInAnyOrder("The Shining", "IT");

        assertThat(jkRowlingBooks).hasSize(1);
        assertThat(jkRowlingBooks.get(0).getName()).isEqualTo("Harry Potter");
    }

    @Test
    void bookCategories_ShouldBeProperlyStored() {
        // When
        List<Book> novels = bookRepository.findAll().stream()
                .filter(book -> book.getCategory() == Category.NOVEL)
                .toList();

        List<Book> fantasyBooks = bookRepository.findAll().stream()
                .filter(book -> book.getCategory() == Category.FANTASY)
                .toList();

        // Then
        assertThat(novels).hasSize(2);
        assertThat(novels).extracting(Book::getName)
                .containsExactlyInAnyOrder("The Shining", "IT");

        assertThat(fantasyBooks).hasSize(1);
        assertThat(fantasyBooks.get(0).getName()).isEqualTo("Harry Potter");
    }

    @Test
    void availableCopies_ShouldBeProperlyTracked() {
        // When
        List<Book> result = bookRepository.findAll();

        // Then
        assertThat(result).extracting(Book::getAvailableCopies)
                .containsExactlyInAnyOrder(5, 10, 3);

        // Test updating available copies
        testBook1.setAvailableCopies(testBook1.getAvailableCopies() - 1);
        Book updatedBook = bookRepository.save(testBook1);
        
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(4);
    }

    @Test
    void save_ShouldHandleNullValues_Appropriately() {
        // Given - Book with minimum required fields
        Book minimalBook = new Book();
        minimalBook.setName("Minimal Book");
        minimalBook.setCategory(Category.NOVEL);
        minimalBook.setAuthor(testAuthor1);
        minimalBook.setAvailableCopies(1);

        // When
        Book savedBook = bookRepository.save(minimalBook);

        // Then
        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getName()).isEqualTo("Minimal Book");
        assertThat(savedBook.getCategory()).isEqualTo(Category.NOVEL);
        assertThat(savedBook.getAuthor()).isNotNull();
        assertThat(savedBook.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void deleteAll_ShouldRemoveAllBooks() {
        // Given
        assertThat(bookRepository.count()).isEqualTo(3);

        // When
        bookRepository.deleteAll();
        entityManager.flush();

        // Then
        assertThat(bookRepository.count()).isEqualTo(0);
        assertThat(bookRepository.findAll()).isEmpty();
    }
}
