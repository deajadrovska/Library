package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayWishlistDto;
import mk.finki.ukim.mk.library.model.domain.*;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.service.application.Impl.WishlistApplicationServiceImpl;
import mk.finki.ukim.mk.library.service.domain.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistApplicationServiceTest {

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistApplicationServiceImpl wishlistApplicationService;

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
        testWishlist.setBooks(Arrays.asList(testBook));
    }

    @Test
    void getActiveWishlist_ShouldReturnDisplayWishlistDto_WhenWishlistExists() {
        // Given
        when(wishlistService.getActiveWishlist("testuser")).thenReturn(Optional.of(testWishlist));

        // When
        Optional<DisplayWishlistDto> result = wishlistApplicationService.getActiveWishlist("testuser");

        // Then
        assertTrue(result.isPresent());
        DisplayWishlistDto dto = result.get();
        assertEquals(testWishlist.getId(), dto.id());
        verify(wishlistService).getActiveWishlist("testuser");
    }

    @Test
    void getActiveWishlist_ShouldReturnEmpty_WhenWishlistDoesNotExist() {
        // Given
        when(wishlistService.getActiveWishlist("testuser")).thenReturn(Optional.empty());

        // When
        Optional<DisplayWishlistDto> result = wishlistApplicationService.getActiveWishlist("testuser");

        // Then
        assertFalse(result.isPresent());
        verify(wishlistService).getActiveWishlist("testuser");
    }

    @Test
    void addBookToWishlist_ShouldReturnDisplayWishlistDto_WhenBookIsAdded() {
        // Given
        when(wishlistService.addBookToWishlist("testuser", 1L)).thenReturn(Optional.of(testWishlist));

        // When
        Optional<DisplayWishlistDto> result = wishlistApplicationService.addBookToWishlist("testuser", 1L);

        // Then
        assertTrue(result.isPresent());
        DisplayWishlistDto dto = result.get();
        assertEquals(testWishlist.getId(), dto.id());
        verify(wishlistService).addBookToWishlist("testuser", 1L);
    }

    @Test
    void addBookToWishlist_ShouldThrowRuntimeException_WhenServiceThrowsException() {
        // Given
        when(wishlistService.addBookToWishlist("testuser", 1L))
                .thenThrow(new RuntimeException("Book not found"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> wishlistApplicationService.addBookToWishlist("testuser", 1L));
        assertEquals("Book not found", exception.getMessage());
        verify(wishlistService).addBookToWishlist("testuser", 1L);
    }

    @Test
    void removeBookFromWishlist_ShouldReturnDisplayWishlistDto_WhenBookIsRemoved() {
        // Given
        when(wishlistService.removeBookFromWishlist("testuser", 1L)).thenReturn(Optional.of(testWishlist));

        // When
        Optional<DisplayWishlistDto> result = wishlistApplicationService.removeBookFromWishlist("testuser", 1L);

        // Then
        assertTrue(result.isPresent());
        DisplayWishlistDto dto = result.get();
        assertEquals(testWishlist.getId(), dto.id());
        verify(wishlistService).removeBookFromWishlist("testuser", 1L);
    }

    @Test
    void removeBookFromWishlist_ShouldReturnEmpty_WhenServiceReturnsEmpty() {
        // Given
        when(wishlistService.removeBookFromWishlist("testuser", 1L)).thenReturn(Optional.empty());

        // When
        Optional<DisplayWishlistDto> result = wishlistApplicationService.removeBookFromWishlist("testuser", 1L);

        // Then
        assertFalse(result.isPresent());
        verify(wishlistService).removeBookFromWishlist("testuser", 1L);
    }

    @Test
    void borrowAllBooks_ShouldReturnDisplayWishlistDto_WhenBooksAreBorrowed() {
        // Given
        when(wishlistService.borrowAllBooks("testuser")).thenReturn(Optional.of(testWishlist));

        // When
        Optional<DisplayWishlistDto> result = wishlistApplicationService.borrowAllBooks("testuser");

        // Then
        assertTrue(result.isPresent());
        DisplayWishlistDto dto = result.get();
        assertEquals(testWishlist.getId(), dto.id());
        verify(wishlistService).borrowAllBooks("testuser");
    }

    @Test
    void borrowAllBooks_ShouldThrowRuntimeException_WhenServiceThrowsException() {
        // Given
        when(wishlistService.borrowAllBooks("testuser"))
                .thenThrow(new RuntimeException("Not enough copies available"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> wishlistApplicationService.borrowAllBooks("testuser"));
        assertEquals("Not enough copies available", exception.getMessage());
        verify(wishlistService).borrowAllBooks("testuser");
    }

    @Test
    void listBooksInWishlist_ShouldReturnDisplayBookDtos() {
        // Given
        List<Book> books = Arrays.asList(testBook);
        when(wishlistService.listBooksInWishlist("testuser")).thenReturn(books);

        // When
        List<DisplayBookDto> result = wishlistApplicationService.listBooksInWishlist("testuser");

        // Then
        assertEquals(1, result.size());
        DisplayBookDto dto = result.get(0);
        assertEquals(testBook.getId(), dto.id());
        assertEquals(testBook.getName(), dto.name());
        verify(wishlistService).listBooksInWishlist("testuser");
    }

    @Test
    void listBooksInWishlist_ShouldReturnEmptyList_WhenNoBooksInWishlist() {
        // Given
        when(wishlistService.listBooksInWishlist("testuser")).thenReturn(Arrays.asList());

        // When
        List<DisplayBookDto> result = wishlistApplicationService.listBooksInWishlist("testuser");

        // Then
        assertTrue(result.isEmpty());
        verify(wishlistService).listBooksInWishlist("testuser");
    }
}
