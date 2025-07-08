package mk.finki.ukim.mk.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.model.Dto.CreateCountryDto;
import mk.finki.ukim.mk.library.model.domain.Country;
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
class CountryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CountryRepository countryRepository;

    private Country testCountry;

    @BeforeEach
    void setUp() {
        // Clean up database
        countryRepository.deleteAll();

        // Create test data
        testCountry = new Country("Test Country", "Test Continent");
        testCountry = countryRepository.save(testCountry);
    }

    @Test
    void findAll_ShouldReturnAllCountries_WhenCountriesExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testCountry.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("Test Country")))
                .andExpect(jsonPath("$[0].continent", is("Test Continent")));
    }

    @Test
    void findById_ShouldReturnCountry_WhenCountryExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/countries/{id}", testCountry.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCountry.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Country")))
                .andExpect(jsonPath("$.continent", is("Test Continent")));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenCountryDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/countries/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void save_ShouldCreateCountry_WhenValidData() throws Exception {
        // Given
        CreateCountryDto createCountryDto = new CreateCountryDto("New Country", "New Continent");

        // When & Then
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCountryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("New Country")))
                .andExpect(jsonPath("$.continent", is("New Continent")));

        // Verify country was saved to database
        assert countryRepository.count() == 2;
    }

    @Test
    void save_ShouldCreateCountry_WhenDuplicateCountryName() throws Exception {
        // Given - using existing country name (application allows duplicates)
        CreateCountryDto createCountryDto = new CreateCountryDto("Test Country", "Different Continent");

        // When & Then
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCountryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Test Country")))
                .andExpect(jsonPath("$.continent", is("Different Continent")));

        // Verify additional country was created (application allows duplicates)
        assert countryRepository.count() == 2;
    }

    @Test
    void update_ShouldUpdateCountry_WhenValidData() throws Exception {
        // Given
        CreateCountryDto updateCountryDto = new CreateCountryDto("Updated Country", "Updated Continent");

        // When & Then
        mockMvc.perform(put("/api/countries/edit/{id}", testCountry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCountryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCountry.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated Country")))
                .andExpect(jsonPath("$.continent", is("Updated Continent")));

        // Verify country was updated in database
        Country updatedCountry = countryRepository.findById(testCountry.getId()).orElseThrow();
        assert updatedCountry.getName().equals("Updated Country");
        assert updatedCountry.getContinent().equals("Updated Continent");
    }

    @Test
    void update_ShouldReturnNotFound_WhenCountryDoesNotExist() throws Exception {
        // Given
        CreateCountryDto updateCountryDto = new CreateCountryDto("Updated Country", "Updated Continent");

        // When & Then
        mockMvc.perform(put("/api/countries/edit/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCountryDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldDeleteCountry_WhenCountryExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/countries/delete/{id}", testCountry.getId()))
                .andExpect(status().isNoContent());

        // Verify country was deleted from database
        assert countryRepository.count() == 0;
        assert !countryRepository.existsById(testCountry.getId());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenCountryDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/countries/delete/{id}", 999L))
                .andExpect(status().isNotFound());

        // Verify original country still exists
        assert countryRepository.count() == 1;
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCountriesExist() throws Exception {
        // Given - clean database
        countryRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void save_ShouldCreateCountry_WhenMissingOptionalFields() throws Exception {
        // Given - missing continent field (application allows null values)
        String incompleteJson = "{\"name\":\"Test\"}";

        // When & Then
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.continent").doesNotExist());
    }

    @Test
    void save_ShouldReturnBadRequest_WhenMalformedJson() throws Exception {
        // Given - malformed JSON
        String malformedJson = "{invalid json}";

        // When & Then
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_ShouldCreateMultipleCountries_WhenValidData() throws Exception {
        // Given
        CreateCountryDto country1 = new CreateCountryDto("Country 1", "Continent 1");
        CreateCountryDto country2 = new CreateCountryDto("Country 2", "Continent 2");

        // When & Then - Create first country
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(country1)))
                .andExpect(status().isOk());

        // When & Then - Create second country
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(country2)))
                .andExpect(status().isOk());

        // Verify both countries were saved
        assert countryRepository.count() == 3; // Including the initial test country
    }

    @Test
    void update_ShouldUpdateCountry_WhenUpdatingToExistingName() throws Exception {
        // Given - create another country first
        Country anotherCountry = new Country("Another Country", "Another Continent");
        anotherCountry = countryRepository.save(anotherCountry);

        // Try to update first country to have the same name as the second (application allows duplicates)
        CreateCountryDto updateCountryDto = new CreateCountryDto("Another Country", "Different Continent");

        // When & Then
        mockMvc.perform(put("/api/countries/edit/{id}", testCountry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCountryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Another Country")))
                .andExpect(jsonPath("$.continent", is("Different Continent")));

        // Verify country was updated (application allows duplicate names)
        Country updatedCountry = countryRepository.findById(testCountry.getId()).orElseThrow();
        assert updatedCountry.getName().equals("Another Country");
        assert updatedCountry.getContinent().equals("Different Continent");
    }
}
