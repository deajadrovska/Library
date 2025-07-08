package mk.finki.ukim.mk.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.repository.CountryRepository;
import mk.finki.ukim.mk.library.repository.UserRepository;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.security.JwtHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
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
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.profiles.active=test"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JwtHelper jwtHelper;

    private Country testCountry;
    private Author testAuthor;
    private Book testBook;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up database
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        countryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user (needed for BookService.save method)
        testUser = new User("dj", "password", "Test", "User", Role.ROLE_LIBRARIAN);
        testUser = userRepository.save(testUser);

        // Create test data
        testCountry = new Country("Test Country", "Test Continent");
        testCountry = countryRepository.save(testCountry);

        testAuthor = new Author("Test", "Author", testCountry);
        testAuthor = authorRepository.save(testAuthor);

        testBook = new Book("Test Book", Category.NOVEL, testAuthor, 5);
        testBook = bookRepository.save(testBook);
    }

    @Test
    void findAll_ShouldReturnAllBooks_WhenBooksExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testBook.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("Test Book")))
                .andExpect(jsonPath("$[0].category", is("NOVEL")))
                .andExpect(jsonPath("$[0].availableCopies", is(5)))
                .andExpect(jsonPath("$[0].author.name", is("Test")))
                .andExpect(jsonPath("$[0].author.surname", is("Author")));
    }

    @Test
    void findById_ShouldReturnBook_WhenBookExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books/{id}", testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBook.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Book")))
                .andExpect(jsonPath("$.category", is("NOVEL")))
                .andExpect(jsonPath("$.availableCopies", is(5)));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void save_ShouldCreateBook_WhenValidData() throws Exception {
        // Given
        CreateBookDto createBookDto = new CreateBookDto(
                "New Book",
                Category.FANTASY,
                testAuthor.getId(),
                10
        );

        // When & Then
        mockMvc.perform(post("/api/books/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("New Book")))
                .andExpect(jsonPath("$.category", is("FANTASY")))
                .andExpect(jsonPath("$.availableCopies", is(10)))
                .andExpect(jsonPath("$.author.id", is(testAuthor.getId().intValue())));

        // Verify book was saved to database
        assert bookRepository.count() == 2;
    }

    @Test
    void save_ShouldReturnBadRequest_WhenAuthorDoesNotExist() throws Exception {
        // Given
        CreateBookDto createBookDto = new CreateBookDto(
                "New Book",
                Category.FANTASY,
                999L, // Non-existent author
                10
        );

        // When & Then
        mockMvc.perform(post("/api/books/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookDto)))
                .andExpect(status().isBadRequest());

        // Verify book was not saved
        assert bookRepository.count() == 1;
    }

    @Test
    void update_ShouldUpdateBook_WhenValidData() throws Exception {
        // Given
        CreateBookDto updateBookDto = new CreateBookDto(
                "Updated Book",
                Category.BIOGRAPHY,
                testAuthor.getId(),
                15
        );

        // When & Then
        mockMvc.perform(put("/api/books/edit/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBookDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBook.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated Book")))
                .andExpect(jsonPath("$.category", is("BIOGRAPHY")))
                .andExpect(jsonPath("$.availableCopies", is(15)));

        // Verify book was updated in database
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert updatedBook.getName().equals("Updated Book");
        assert updatedBook.getCategory() == Category.BIOGRAPHY;
        assert updatedBook.getAvailableCopies() == 15;
    }

    @Test
    void update_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // Given
        CreateBookDto updateBookDto = new CreateBookDto(
                "Updated Book",
                Category.BIOGRAPHY,
                testAuthor.getId(),
                15
        );

        // When & Then
        mockMvc.perform(put("/api/books/edit/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBookDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldDeleteBook_WhenBookExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/books/delete/{id}", testBook.getId()))
                .andExpect(status().isNoContent()); // 204 is the correct status for successful DELETE

        // Verify book was deleted from database
        assert bookRepository.count() == 0;
        assert !bookRepository.existsById(testBook.getId());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/books/delete/{id}", 999L))
                .andExpect(status().isNotFound());

        // Verify original book still exists
        assert bookRepository.count() == 1;
    }

    @Test
    void markAsBorrowed_ShouldDecreaseAvailableCopies_WhenBookExists() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/books/{id}/mark-as-borrowed", testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBook.getId().intValue())))
                .andExpect(jsonPath("$.availableCopies", is(4))); // Decreased from 5 to 4

        // Verify available copies decreased in database
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert updatedBook.getAvailableCopies() == 4;
    }

    @Test
    void markAsBorrowed_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/books/{id}/mark-as-borrowed", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllCategories_ShouldReturnAllCategories() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(7))) // All Category enum values
                .andExpect(jsonPath("$", hasItem("NOVEL")))
                .andExpect(jsonPath("$", hasItem("THRILER")))
                .andExpect(jsonPath("$", hasItem("HISTORY")))
                .andExpect(jsonPath("$", hasItem("FANTASY")))
                .andExpect(jsonPath("$", hasItem("BIOGRAPHY")))
                .andExpect(jsonPath("$", hasItem("CLASSICS")))
                .andExpect(jsonPath("$", hasItem("DRAMA")));
    }
}
