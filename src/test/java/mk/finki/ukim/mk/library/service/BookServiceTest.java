package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.domain.*;
import mk.finki.ukim.mk.library.repository.BookHistoryRepository;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.repository.BooksByAuthorViewRepository;
import mk.finki.ukim.mk.library.service.domain.Impl.BookServiceImpl;
import mk.finki.ukim.mk.library.service.domain.UserService;
import mk.finki.ukim.mk.library.config.UserContext;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookHistoryRepository bookHistoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private BooksByAuthorViewRepository booksByAuthorViewRepository;

    @Mock
    private UserContext userContext;

    @Mock
    private Environment environment;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private Author testAuthor;
    private Country testCountry;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCountry = new Country("Test Country", "Test Continent");
        testCountry.setId(1L);

        testAuthor = new Author("Test", "Author", testCountry);
        testAuthor.setId(1L);

        testBook = new Book("Test Book", Category.NOVEL, testAuthor, 5);
        testBook.setId(1L);

        testUser = new User("testuser", "password", "Test", "User", Role.ROLE_LIBRARIAN);
    }

    @Test
    void findAll_ShouldReturnAllBooks() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // When
        List<Book> actualBooks = bookService.findAll();

        // Then
        assertEquals(expectedBooks, actualBooks);
        verify(bookRepository).findAll();
    }

    @Test
    void findById_ShouldReturnBook_WhenBookExists() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());
        verify(bookRepository).findById(1L);
    }

    @Test
    void save_ShouldSaveBookAndCreateHistory() {
        // Given
        String username = "testuser";
        when(userService.findByUsername(username)).thenReturn(testUser);
        when(bookRepository.save(testBook)).thenReturn(testBook);
        when(environment.acceptsProfiles("test")).thenReturn(true);

        // When
        Optional<Book> result = bookService.save(testBook, username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());
        verify(userService).findByUsername(username);
        verify(bookRepository).save(testBook);
        verify(bookHistoryRepository).save(any(BookHistory.class));
        // In test environment, refreshBooksByAuthorView should not call refreshMaterializedViewPostgreSQL
        verify(environment).acceptsProfiles("test");
    }

    @Test
    void markAsBorrowed_ShouldDecreaseAvailableCopies_WhenCopiesAvailable() {
        // Given
        testBook.setAvailableCopies(3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Optional<Book> result = bookService.markAsBorrowed(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getAvailableCopies());
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(testBook);
    }
}