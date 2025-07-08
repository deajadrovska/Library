package mk.finki.ukim.mk.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.Dto.CreateAuthorDto;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class AuthorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Country testCountry;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        // Clean up database
        authorRepository.deleteAll();
        countryRepository.deleteAll();

        // Create test data
        testCountry = new Country("Test Country", "Test Continent");
        testCountry = countryRepository.save(testCountry);

        testAuthor = new Author("Test", "Author", testCountry);
        testAuthor = authorRepository.save(testAuthor);
    }

    @Test
    void findAll_ShouldReturnAllAuthors_WhenAuthorsExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testAuthor.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("Test")))
                .andExpect(jsonPath("$[0].surname", is("Author")))
                .andExpect(jsonPath("$[0].country.name", is("Test Country")))
                .andExpect(jsonPath("$[0].country.continent", is("Test Continent")));
    }

    @Test
    void findById_ShouldReturnAuthor_WhenAuthorExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/{id}", testAuthor.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testAuthor.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.surname", is("Author")))
                .andExpect(jsonPath("$.country.name", is("Test Country")));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenAuthorDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void save_ShouldCreateAuthor_WhenValidData() throws Exception {
        // Given
        CreateAuthorDto createAuthorDto = new CreateAuthorDto(
                "New",
                "Author",
                testCountry.getId()
        );

        // When & Then
        mockMvc.perform(post("/api/authors/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("New")))
                .andExpect(jsonPath("$.surname", is("Author")))
                .andExpect(jsonPath("$.country.id", is(testCountry.getId().intValue())));

        // Verify author was saved to database
        assert authorRepository.count() == 2;
    }

    @Test
    void save_ShouldReturnBadRequest_WhenCountryDoesNotExist() throws Exception {
        // Given
        CreateAuthorDto createAuthorDto = new CreateAuthorDto(
                "New",
                "Author",
                999L // Non-existent country
        );

        // When & Then
        mockMvc.perform(post("/api/authors/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isBadRequest());

        // Verify author was not saved
        assert authorRepository.count() == 1;
    }

    @Test
    void update_ShouldUpdateAuthor_WhenValidData() throws Exception {
        // Given
        CreateAuthorDto updateAuthorDto = new CreateAuthorDto(
                "Updated",
                "Author",
                testCountry.getId()
        );

        // When & Then
        mockMvc.perform(put("/api/authors/edit/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testAuthor.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.surname", is("Author")));

        // Verify author was updated in database
        Author updatedAuthor = authorRepository.findById(testAuthor.getId()).orElseThrow();
        assert updatedAuthor.getName().equals("Updated");
    }

    @Test
    void update_ShouldReturnNotFound_WhenAuthorDoesNotExist() throws Exception {
        // Given
        CreateAuthorDto updateAuthorDto = new CreateAuthorDto(
                "Updated",
                "Author",
                testCountry.getId()
        );

        // When & Then
        mockMvc.perform(put("/api/authors/edit/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAuthorDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldDeleteAuthor_WhenAuthorExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/authors/delete/{id}", testAuthor.getId()))
                .andExpect(status().isNoContent()); // 204 is the correct status for successful DELETE

        // Verify author was deleted from database
        assert authorRepository.count() == 0;
        assert !authorRepository.existsById(testAuthor.getId());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenAuthorDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/authors/delete/{id}", 999L))
                .andExpect(status().isNotFound());

        // Verify original author still exists
        assert authorRepository.count() == 1;
    }

    @Test
    void getAuthorsCountByCountry_ShouldReturnCountsByCountry() throws Exception {
        // Create additional authors in different countries
        Country anotherCountry = new Country("Another Country", "Another Continent");
        anotherCountry = countryRepository.save(anotherCountry);
        
        Author anotherAuthor = new Author("Another", "Author", anotherCountry);
        authorRepository.save(anotherAuthor);

        // When & Then
        mockMvc.perform(get("/api/authors/by-country"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void getAuthorNames_ShouldReturnAuthorNamesProjection() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/names"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test")))
                .andExpect(jsonPath("$[0].surname", is("Author")));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoAuthorsExist() throws Exception {
        // Given - clean database
        authorRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void save_ShouldCreateAuthor_WhenEmptyFields() throws Exception {
        // Given - CreateAuthorDto with empty name and surname (application allows this)
        CreateAuthorDto emptyFieldsDto = new CreateAuthorDto("", "", testCountry.getId());

        // When & Then
        mockMvc.perform(post("/api/authors/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyFieldsDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("")))
                .andExpect(jsonPath("$.surname", is("")))
                .andExpect(jsonPath("$.country.id", is(testCountry.getId().intValue())));

        // Verify author was saved
        assert authorRepository.count() == 2; // testAuthor + new author
    }

    @Test
    void save_ShouldReturnBadRequest_WhenMalformedJson() throws Exception {
        // Given - malformed JSON
        String malformedJson = "{invalid json}";

        // When & Then
        mockMvc.perform(post("/api/authors/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}
