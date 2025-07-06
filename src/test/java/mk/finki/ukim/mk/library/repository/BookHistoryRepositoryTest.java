package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.domain.*;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test")
class BookHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookHistoryRepository bookHistoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Book testBook1;
    private Book testBook2;
    private User testUser1;
    private User testUser2;
    private BookHistory history1;
    private BookHistory history2;
    private BookHistory history3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        bookHistoryRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        authorRepository.deleteAll();
        countryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test data
        Country country = new Country("Test Country", "Test Continent");
        entityManager.persistAndFlush(country);

        Author author = new Author("Test", "Author", country);
        entityManager.persistAndFlush(author);

        testBook1 = new Book("Test Book 1", Category.NOVEL, author, 5);
        testBook2 = new Book("Test Book 2", Category.BIOGRAPHY, author, 3);
        entityManager.persistAndFlush(testBook1);
        entityManager.persistAndFlush(testBook2);

        testUser1 = new User("user1", "password1", "User", "One", Role.ROLE_USER);
        testUser2 = new User("user2", "password2", "User", "Two", Role.ROLE_LIBRARIAN);
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // Create book history entries with different timestamps
        LocalDateTime now = LocalDateTime.now();
        
        history1 = new BookHistory(testBook1, testUser1);
        history1.setModifiedAt(now.minusDays(3));
        
        history2 = new BookHistory(testBook1, testUser2);
        history2.setModifiedAt(now.minusDays(1));
        
        history3 = new BookHistory(testBook2, testUser1);
        history3.setModifiedAt(now.minusDays(2));

        entityManager.persistAndFlush(history1);
        entityManager.persistAndFlush(history2);
        entityManager.persistAndFlush(history3);
        entityManager.clear();
    }

    @Test
    void findByBookOrderByModifiedAtDesc_ShouldReturnHistoryInDescendingOrder() {
        // When
        List<BookHistory> result = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook1);

        // Then
        assertThat(result).hasSize(2);
        
        // Verify ordering - most recent first
        assertThat(result.get(0).getModifiedBy().getUsername()).isEqualTo("user2");
        assertThat(result.get(1).getModifiedBy().getUsername()).isEqualTo("user1");
        
        // Verify timestamps are in descending order
        assertThat(result.get(0).getModifiedAt()).isAfter(result.get(1).getModifiedAt());
        
        // Verify all entries are for the correct book
        result.forEach(history -> {
            assertThat(history.getBook().getId()).isEqualTo(testBook1.getId());
            assertThat(history.getBook().getName()).isEqualTo("Test Book 1");
        });
    }

    @Test
    void findByBookOrderByModifiedAtDesc_ShouldReturnSingleEntry_WhenBookHasOneHistory() {
        // When
        List<BookHistory> result = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook2);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBook().getId()).isEqualTo(testBook2.getId());
        assertThat(result.get(0).getModifiedBy().getUsername()).isEqualTo("user1");
    }

    @Test
    void findByBookOrderByModifiedAtDesc_ShouldReturnEmptyList_WhenBookHasNoHistory() {
        // Given
        Book newBook = new Book("New Book", Category.DRAMA, testBook1.getAuthor(), 2);
        entityManager.persistAndFlush(newBook);

        // When
        List<BookHistory> result = bookHistoryRepository.findByBookOrderByModifiedAtDesc(newBook);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByBookId_ShouldRemoveAllHistoryForBook() {
        // Given
        Long bookId = testBook1.getId();
        
        // Verify initial state
        List<BookHistory> initialHistory = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook1);
        assertThat(initialHistory).hasSize(2);

        // When
        bookHistoryRepository.deleteByBookId(bookId);
        entityManager.flush();

        // Then
        List<BookHistory> remainingHistory = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook1);
        assertThat(remainingHistory).isEmpty();

        // Verify other book's history is not affected
        List<BookHistory> otherBookHistory = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook2);
        assertThat(otherBookHistory).hasSize(1);
    }

    @Test
    void deleteByBookId_ShouldNotAffectOtherBooks_WhenBookIdNotFound() {
        // Given
        Long nonExistentBookId = 999L;
        long initialCount = bookHistoryRepository.count();

        // When
        bookHistoryRepository.deleteByBookId(nonExistentBookId);
        entityManager.flush();

        // Then
        assertThat(bookHistoryRepository.count()).isEqualTo(initialCount);
    }

    @Test
    void save_ShouldPersistBookHistory_WhenValidHistoryProvided() {
        // Given
        BookHistory newHistory = new BookHistory(testBook2, testUser2);

        // When
        BookHistory savedHistory = bookHistoryRepository.save(newHistory);

        // Then
        assertThat(savedHistory).isNotNull();
        assertThat(savedHistory.getId()).isNotNull();
        assertThat(savedHistory.getBook().getId()).isEqualTo(testBook2.getId());
        assertThat(savedHistory.getModifiedBy().getUsername()).isEqualTo("user2");
        assertThat(savedHistory.getModifiedAt()).isNotNull();

        // Verify persistence
        List<BookHistory> bookHistory = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook2);
        assertThat(bookHistory).hasSize(2);
    }

    @Test
    void findAll_ShouldReturnAllBookHistoryEntries() {
        // When
        List<BookHistory> result = bookHistoryRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        
        // Verify all entries have required fields
        result.forEach(history -> {
            assertThat(history.getBook()).isNotNull();
            assertThat(history.getModifiedBy()).isNotNull();
            assertThat(history.getModifiedAt()).isNotNull();
        });
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When & Then
        assertThat(bookHistoryRepository.count()).isEqualTo(3);
    }

    @Test
    void delete_ShouldRemoveSpecificHistoryEntry() {
        // Given
        Long historyId = history1.getId();
        assertThat(bookHistoryRepository.existsById(historyId)).isTrue();

        // When
        bookHistoryRepository.deleteById(historyId);
        entityManager.flush();

        // Then
        assertThat(bookHistoryRepository.existsById(historyId)).isFalse();
        assertThat(bookHistoryRepository.count()).isEqualTo(2);

        // Verify remaining entries for the same book
        List<BookHistory> remainingHistory = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook1);
        assertThat(remainingHistory).hasSize(1);
        assertThat(remainingHistory.get(0).getModifiedBy().getUsername()).isEqualTo("user2");
    }

    @Test
    void bookHistoryRelationships_ShouldBeProperlyMaintained() {
        // When
        List<BookHistory> book1History = bookHistoryRepository.findByBookOrderByModifiedAtDesc(testBook1);

        // Then
        assertThat(book1History).hasSize(2);
        
        // Verify book relationship
        book1History.forEach(history -> {
            assertThat(history.getBook().getName()).isEqualTo("Test Book 1");
            assertThat(history.getBook().getCategory()).isEqualTo(Category.NOVEL);
            assertThat(history.getBook().getAuthor().getName()).isEqualTo("Test");
        });

        // Verify user relationships
        assertThat(book1History).extracting(history -> history.getModifiedBy().getUsername())
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void modifiedAtTimestamp_ShouldBeAutomaticallySet() {
        // Given
        BookHistory newHistory = new BookHistory(testBook1, testUser1);
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);

        // When
        BookHistory savedHistory = bookHistoryRepository.save(newHistory);
        LocalDateTime afterSave = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(savedHistory.getModifiedAt()).isNotNull();
        assertThat(savedHistory.getModifiedAt()).isAfter(beforeSave);
        assertThat(savedHistory.getModifiedAt()).isBefore(afterSave);
    }
}
