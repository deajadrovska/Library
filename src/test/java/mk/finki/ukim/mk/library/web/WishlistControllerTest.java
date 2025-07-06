package mk.finki.ukim.mk.library.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayWishlistDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.model.enumerations.WishlistStatus;
import mk.finki.ukim.mk.library.service.application.WishlistApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WishlistController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test")
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistApplicationService wishlistApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private DisplayWishlistDto testWishlistDto;
    private DisplayBookDto testBookDto;
    private List<DisplayBookDto> testBooks;

    @BeforeEach
    void setUp() {
        DisplayCountryDto countryDto = new DisplayCountryDto(1L, "Test Country", "Test Continent");
        DisplayAuthorDto authorDto = new DisplayAuthorDto(1L, "Test", "Author", countryDto);
        
        testBookDto = new DisplayBookDto(1L, "Test Book", Category.NOVEL, authorDto, 5);
        DisplayBookDto testBookDto2 = new DisplayBookDto(2L, "Another Book", Category.BIOGRAPHY, authorDto, 3);
        
        testBooks = Arrays.asList(testBookDto, testBookDto2);
        
        testWishlistDto = new DisplayWishlistDto(
                1L,
                "testuser",
                testBooks,
                LocalDateTime.now(),
                WishlistStatus.CREATED
        );
    }

    @Test
    @WithMockUser(username = "testuser")
    void getWishlist_ShouldReturnWishlist_WhenWishlistExists() throws Exception {
        // Given
        when(wishlistApplicationService.getActiveWishlist("testuser"))
                .thenReturn(Optional.of(testWishlistDto));

        // When & Then
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books.length()").value(2))
                .andExpect(jsonPath("$.books[0].id").value(1L))
                .andExpect(jsonPath("$.books[0].name").value("Test Book"))
                .andExpect(jsonPath("$.books[1].id").value(2L))
                .andExpect(jsonPath("$.books[1].name").value("Another Book"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getWishlist_ShouldReturnNotFound_WhenWishlistDoesNotExist() throws Exception {
        // Given
        when(wishlistApplicationService.getActiveWishlist("testuser"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isNotFound());
    }



    @Test
    @WithMockUser(username = "testuser")
    void addBookToWishlist_ShouldReturnWishlist_WhenBookAddedSuccessfully() throws Exception {
        // Given
        when(wishlistApplicationService.addBookToWishlist("testuser", 1L))
                .thenReturn(Optional.of(testWishlistDto));

        // When & Then
        mockMvc.perform(post("/api/wishlist/add/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.books.length()").value(2));
    }

    @Test
    @WithMockUser(username = "testuser")
    void addBookToWishlist_ShouldReturnBadRequest_WhenBookCannotBeAdded() throws Exception {
        // Given
        when(wishlistApplicationService.addBookToWishlist("testuser", 1L))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/wishlist/add/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void addBookToWishlist_ShouldReturnBadRequestWithMessage_WhenExceptionThrown() throws Exception {
        // Given
        String errorMessage = "Book not available";
        when(wishlistApplicationService.addBookToWishlist("testuser", 1L))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(post("/api/wishlist/add/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }



    @Test
    @WithMockUser(username = "testuser")
    void removeBookFromWishlist_ShouldReturnWishlist_WhenBookRemovedSuccessfully() throws Exception {
        // Given
        DisplayWishlistDto updatedWishlist = new DisplayWishlistDto(
                1L, "testuser", Arrays.asList(testBooks.get(1)),
                LocalDateTime.now(), WishlistStatus.CREATED
        );
        when(wishlistApplicationService.removeBookFromWishlist("testuser", 1L))
                .thenReturn(Optional.of(updatedWishlist));

        // When & Then
        mockMvc.perform(delete("/api/wishlist/remove/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.books.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void removeBookFromWishlist_ShouldReturnBadRequest_WhenBookCannotBeRemoved() throws Exception {
        // Given
        when(wishlistApplicationService.removeBookFromWishlist("testuser", 1L))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/wishlist/remove/1"))
                .andExpect(status().isBadRequest());
    }



    @Test
    @WithMockUser(username = "testuser")
    void borrowAllBooks_ShouldReturnWishlist_WhenBorrowedSuccessfully() throws Exception {
        // Given
        DisplayWishlistDto borrowedWishlist = new DisplayWishlistDto(
                1L, "testuser", Arrays.asList(), 
                LocalDateTime.now(), WishlistStatus.BORROWED
        );
        when(wishlistApplicationService.borrowAllBooks("testuser"))
                .thenReturn(Optional.of(borrowedWishlist));

        // When & Then
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.status").value("BORROWED"))
                .andExpect(jsonPath("$.books.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void borrowAllBooks_ShouldReturnBadRequest_WhenCannotBorrow() throws Exception {
        // Given
        when(wishlistApplicationService.borrowAllBooks("testuser"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void borrowAllBooks_ShouldReturnBadRequestWithMessage_WhenExceptionThrown() throws Exception {
        // Given
        String errorMessage = "Insufficient copies available";
        when(wishlistApplicationService.borrowAllBooks("testuser"))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }



    @Test
    @WithMockUser(username = "testuser")
    void listBooksInWishlist_ShouldReturnBooksList_WhenBooksExist() throws Exception {
        // Given
        when(wishlistApplicationService.listBooksInWishlist("testuser"))
                .thenReturn(testBooks);

        // When & Then
        mockMvc.perform(get("/api/wishlist/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Book"))
                .andExpect(jsonPath("$[0].category").value("NOVEL"))
                .andExpect(jsonPath("$[0].availableCopies").value(5))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Another Book"))
                .andExpect(jsonPath("$[1].category").value("BIOGRAPHY"))
                .andExpect(jsonPath("$[1].availableCopies").value(3));
    }

    @Test
    @WithMockUser(username = "testuser")
    void listBooksInWishlist_ShouldReturnEmptyList_WhenNoBooksInWishlist() throws Exception {
        // Given
        when(wishlistApplicationService.listBooksInWishlist("testuser"))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/wishlist/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }


}
