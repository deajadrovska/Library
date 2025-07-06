package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.domain.*;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.model.enumerations.WishlistStatus;
import mk.finki.ukim.mk.library.repository.WishlistRepository;
import mk.finki.ukim.mk.library.service.domain.BookService;
import mk.finki.ukim.mk.library.service.domain.Impl.WishlistServiceImpl;
import mk.finki.ukim.mk.library.service.domain.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User testUser;
    private Book testBook;
    private Wishlist testWishlist;
    private Country testCountry;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password", "Test", "User", Role.ROLE_USER);
        
        testCountry = new Country("Test Country", "Test Continent");
        testCountry.setId(1L);
        
        testAuthor = new Author("Test", "Author", testCountry);
        testAuthor.setId(1L);
        
        testBook = new Book("Test Book", Category.NOVEL, testAuthor, 5);
        testBook.setId(1L);
        
        testWishlist = new Wishlist(testUser);
        testWishlist.setId(1L);
    }

    @Test
    void getActiveWishlist_ShouldReturnExistingWishlist_WhenWishlistExists() {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.of(testWishlist));

        // When
        Optional<Wishlist> result = wishlistService.getActiveWishlist("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testWishlist, result.get());
        verify(userService).findByUsername("testuser");
        verify(wishlistRepository).findByUserAndStatus(testUser, WishlistStatus.CREATED);
    }

    @Test
    void getActiveWishlist_ShouldCreateNewWishlist_WhenNoActiveWishlistExists() {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        // When
        Optional<Wishlist> result = wishlistService.getActiveWishlist("testuser");

        // Then
        assertTrue(result.isPresent());
        verify(userService).findByUsername("testuser");
        verify(wishlistRepository).findByUserAndStatus(testUser, WishlistStatus.CREATED);
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void getActiveWishlist_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> wishlistService.getActiveWishlist("nonexistent"));
        assertEquals("User not found", exception.getMessage());
        verify(userService).findByUsername("nonexistent");
    }

    @Test
    void addBookToWishlist_ShouldAddBookToWishlist_WhenBookIsAvailable() {
        // Given
        testWishlist.setBooks(new ArrayList<>());
        when(bookService.findById(1L)).thenReturn(Optional.of(testBook));
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.of(testWishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        // When
        Optional<Wishlist> result = wishlistService.addBookToWishlist("testuser", 1L);

        // Then
        assertTrue(result.isPresent());
        assertTrue(testWishlist.getBooks().contains(testBook));
        verify(bookService).findById(1L);
        verify(wishlistRepository).save(testWishlist);
    }

    @Test
    void addBookToWishlist_ShouldThrowException_WhenBookNotFound() {
        // Given
        when(bookService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> wishlistService.addBookToWishlist("testuser", 999L));
        assertEquals("Book not found", exception.getMessage());
        verify(bookService).findById(999L);
    }

    @Test
    void addBookToWishlist_ShouldThrowException_WhenNoAvailableCopies() {
        // Given
        testBook.setAvailableCopies(0);
        when(bookService.findById(1L)).thenReturn(Optional.of(testBook));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> wishlistService.addBookToWishlist("testuser", 1L));
        assertEquals("No available copies for this book", exception.getMessage());
        verify(bookService).findById(1L);
    }

    @Test
    void removeBookFromWishlist_ShouldRemoveBookFromWishlist() {
        // Given
        List<Book> books = new ArrayList<>(Arrays.asList(testBook));
        testWishlist.setBooks(books);
        when(bookService.findById(1L)).thenReturn(Optional.of(testBook));
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.of(testWishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        // When
        Optional<Wishlist> result = wishlistService.removeBookFromWishlist("testuser", 1L);

        // Then
        assertTrue(result.isPresent());
        assertFalse(testWishlist.getBooks().contains(testBook));
        verify(bookService).findById(1L);
        verify(wishlistRepository).save(testWishlist);
    }

    @Test
    void borrowAllBooks_ShouldBorrowAllBooksAndClearWishlist() {
        // Given
        List<Book> books = new ArrayList<>(Arrays.asList(testBook));
        testWishlist.setBooks(books);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.of(testWishlist));
        when(bookService.markAsBorrowed(1L)).thenReturn(Optional.of(testBook));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        // When
        Optional<Wishlist> result = wishlistService.borrowAllBooks("testuser");

        // Then
        assertTrue(result.isPresent());
        assertTrue(testWishlist.getBooks().isEmpty());
        verify(bookService).markAsBorrowed(1L);
        verify(wishlistRepository).save(testWishlist);
    }

    @Test
    void borrowAllBooks_ShouldThrowException_WhenBookNotAvailable() {
        // Given
        testBook.setAvailableCopies(0);
        List<Book> books = new ArrayList<>(Arrays.asList(testBook));
        testWishlist.setBooks(books);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.of(testWishlist));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> wishlistService.borrowAllBooks("testuser"));
        assertEquals("Not enough copies available for book: Test Book", exception.getMessage());
    }

    @Test
    void createWishlist_ShouldCreateAndReturnNewWishlist() {
        // Given
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        // When
        Optional<Wishlist> result = wishlistService.createWishlist(testUser);

        // Then
        assertTrue(result.isPresent());
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void listBooksInWishlist_ShouldReturnBooksFromActiveWishlist() {
        // Given
        List<Book> books = Arrays.asList(testBook);
        testWishlist.setBooks(books);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.of(testWishlist));

        // When
        List<Book> result = wishlistService.listBooksInWishlist("testuser");

        // Then
        assertEquals(books, result);
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
    }

    @Test
    void listBooksInWishlist_ShouldReturnEmptyList_WhenNoActiveWishlist() {
        // Given
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(wishlistRepository.findByUserAndStatus(testUser, WishlistStatus.CREATED))
                .thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        // When
        List<Book> result = wishlistService.listBooksInWishlist("testuser");

        // Then
        assertTrue(result.isEmpty());
    }
}
