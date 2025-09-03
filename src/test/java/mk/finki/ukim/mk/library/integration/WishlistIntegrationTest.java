package mk.finki.ukim.mk.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.domain.*;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.model.enumerations.WishlistStatus;
import mk.finki.ukim.mk.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {LibraryApplication.class, TestSecurityConfig.class},
        properties = {
                "spring.profiles.active=test",
                "spring.main.allow-bean-definition-overriding=true"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class WishlistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Country testCountry;
    private Author testAuthor;
    private Book testBook1;
    private Book testBook2;
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        // Clean up database
        wishlistRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        countryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test data
        testUser = new User("testuser", passwordEncoder.encode("password123"), "Test", "User", Role.ROLE_USER);
        testUser = userRepository.save(testUser);

        testCountry = new Country("Test Country", "Test Continent");
        testCountry = countryRepository.save(testCountry);

        testAuthor = new Author("Test", "Author", testCountry);
        testAuthor = authorRepository.save(testAuthor);

        testBook1 = new Book("Test Book 1", Category.NOVEL, testAuthor, 5);
        testBook1 = bookRepository.save(testBook1);

        testBook2 = new Book("Test Book 2", Category.FANTASY, testAuthor, 3);
        testBook2 = bookRepository.save(testBook2);

        testWishlist = new Wishlist(testUser);
        testWishlist.getBooks().add(testBook1);
        testWishlist = wishlistRepository.save(testWishlist);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getWishlist_ShouldReturnWishlist_WhenWishlistExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testWishlist.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.status", is("CREATED")))
                .andExpect(jsonPath("$.books", hasSize(1)))
                .andExpect(jsonPath("$.books[0].id", is(testBook1.getId().intValue())))
                .andExpect(jsonPath("$.books[0].name", is("Test Book 1")))
                .andExpect(jsonPath("$.dateCreated", notNullValue()));
    }



    @Test
    void getWishlist_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void addBookToWishlist_ShouldAddBook_WhenBookExists() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/wishlist/add/{bookId}", testBook2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testWishlist.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.books", hasSize(2)))
                .andExpect(jsonPath("$.books[*].id", hasItems(
                        testBook1.getId().intValue(),
                        testBook2.getId().intValue()
                )));

        // Verify book was added to database
        Wishlist updatedWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert updatedWishlist.getBooks().size() == 2;
    }

    @Test
    @WithMockUser(username = "testuser")
    void addBookToWishlist_ShouldReturnBadRequest_WhenBookDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/wishlist/add/{bookId}", 999L))
                .andExpect(status().isBadRequest());

        // Verify wishlist was not modified
        Wishlist unchangedWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert unchangedWishlist.getBooks().size() == 1;
    }

    @Test
    @WithMockUser(username = "testuser")
    void addBookToWishlist_ShouldReturnOk_WhenBookAlreadyInWishlist() throws Exception {
        // When & Then - try to add book that's already in wishlist (should be idempotent)
        mockMvc.perform(post("/api/wishlist/add/{bookId}", testBook1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.books.length()").value(1));

        // Verify wishlist was not modified (still has 1 book)
        Wishlist unchangedWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert unchangedWishlist.getBooks().size() == 1;
    }

    @Test
    void addBookToWishlist_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/wishlist/add/{bookId}", testBook2.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void removeBookFromWishlist_ShouldRemoveBook_WhenBookExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/wishlist/remove/{bookId}", testBook1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testWishlist.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.books", hasSize(0)));

        // Verify book was removed from database
        Wishlist updatedWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert updatedWishlist.getBooks().size() == 0;
    }

    @Test
    @WithMockUser(username = "testuser")
    void removeBookFromWishlist_ShouldReturnOk_WhenBookNotInWishlist() throws Exception {
        // When & Then - removing non-existent book should be idempotent
        mockMvc.perform(delete("/api/wishlist/remove/{bookId}", testBook2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.books.length()").value(1));

        // Verify wishlist was not modified (still has 1 book)
        Wishlist unchangedWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert unchangedWishlist.getBooks().size() == 1;
    }

    @Test
    void removeBookFromWishlist_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/wishlist/remove/{bookId}", testBook1.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void markAsBorrowed_ShouldUpdateWishlistStatus_WhenWishlistExists() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testWishlist.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.status", is("BORROWED")))
                .andExpect(jsonPath("$.books", hasSize(0))); // Books should be cleared when borrowed

        // Verify status was updated in database
        Wishlist updatedWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert updatedWishlist.getStatus() == WishlistStatus.BORROWED;
        assert updatedWishlist.getBooks().isEmpty();

        // Verify book available copies were decreased
        Book updatedBook = bookRepository.findById(testBook1.getId()).orElseThrow();
        assert updatedBook.getAvailableCopies() == 4; // Decreased from 5 to 4
    }

    @Test
    @WithMockUser(username = "testuser")
    void markAsBorrowed_ShouldReturnBadRequest_WhenInsufficientCopies() throws Exception {
        // Given - set book available copies to 0
        testBook1.setAvailableCopies(0);
        bookRepository.save(testBook1);

        // When & Then
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isBadRequest());

        // Verify wishlist status was not changed
        Wishlist unchangedWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert unchangedWishlist.getStatus() == WishlistStatus.CREATED;
    }

    @Test
    void markAsBorrowed_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void markAsBorrowed_ShouldCreateNewWishlist_WhenWishlistDoesNotExist() throws Exception {
        // Given - delete the wishlist
        wishlistRepository.deleteAll();

        // When & Then - should create new empty wishlist and return it
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.books.length()").value(0))
                .andExpect(jsonPath("$.status").value("BORROWED"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void addMultipleBooksToWishlist_ShouldAddAllBooks_WhenBooksExist() throws Exception {
        // When - Add second book
        mockMvc.perform(post("/api/wishlist/add/{bookId}", testBook2.getId()))
                .andExpect(status().isOk());

        // Then - Verify both books are in wishlist
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books", hasSize(2)))
                .andExpect(jsonPath("$.books[*].id", hasItems(
                        testBook1.getId().intValue(),
                        testBook2.getId().intValue()
                )));
    }

    @Test
    @WithMockUser(username = "testuser")
    void wishlistWorkflow_ShouldWorkEndToEnd() throws Exception {
        // 1. Get initial wishlist (has 1 book)
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books", hasSize(1)));

        // 2. Add another book
        mockMvc.perform(post("/api/wishlist/add/{bookId}", testBook2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books", hasSize(2)));

        // 3. Remove first book
        mockMvc.perform(delete("/api/wishlist/remove/{bookId}", testBook1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books", hasSize(1)));

        // 4. Mark as borrowed
        mockMvc.perform(post("/api/wishlist/borrow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("BORROWED")))
                .andExpect(jsonPath("$.books", hasSize(0)));

        // 5. Verify final state
        Wishlist finalWishlist = wishlistRepository.findById(testWishlist.getId()).orElseThrow();
        assert finalWishlist.getStatus() == WishlistStatus.BORROWED;
        assert finalWishlist.getBooks().isEmpty();
    }
}
