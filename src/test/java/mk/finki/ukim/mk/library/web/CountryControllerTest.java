package mk.finki.ukim.mk.library.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateCountryDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;
import mk.finki.ukim.mk.library.service.application.CountryApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CountryController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test")
class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CountryApplicationService countryApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private DisplayCountryDto testCountryDto;
    private CreateCountryDto createCountryDto;

    @BeforeEach
    void setUp() {
        testCountryDto = new DisplayCountryDto(1L, "Test Country", "Test Continent");
        createCountryDto = new CreateCountryDto("Test Country", "Test Continent");
    }

    @Test
    void findAll_ShouldReturnAllCountries() throws Exception {
        // Given
        List<DisplayCountryDto> countries = Arrays.asList(testCountryDto);
        when(countryApplicationService.findAll()).thenReturn(countries);

        // When & Then
        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Country")))
                .andExpect(jsonPath("$[0].continent", is("Test Continent")));

        verify(countryApplicationService).findAll();
    }

    @Test
    void findById_ShouldReturnCountry_WhenCountryExists() throws Exception {
        // Given
        when(countryApplicationService.findById(1L)).thenReturn(Optional.of(testCountryDto));

        // When & Then
        mockMvc.perform(get("/api/countries/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Country")))
                .andExpect(jsonPath("$.continent", is("Test Continent")));

        verify(countryApplicationService).findById(1L);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenCountryDoesNotExist() throws Exception {
        // Given
        when(countryApplicationService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/countries/999"))
                .andExpect(status().isNotFound());

        verify(countryApplicationService).findById(999L);
    }

    @Test
    void save_ShouldCreateCountry_WhenValidData() throws Exception {
        // Given
        when(countryApplicationService.save(any(CreateCountryDto.class))).thenReturn(Optional.of(testCountryDto));

        // When & Then
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCountryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Country")))
                .andExpect(jsonPath("$.continent", is("Test Continent")));

        verify(countryApplicationService).save(any(CreateCountryDto.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        when(countryApplicationService.save(any(CreateCountryDto.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/countries/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCountryDto)))
                .andExpect(status().isBadRequest());

        verify(countryApplicationService).save(any(CreateCountryDto.class));
    }

    @Test
    void update_ShouldUpdateCountry_WhenValidData() throws Exception {
        // Given
        when(countryApplicationService.update(eq(1L), any(CreateCountryDto.class)))
                .thenReturn(Optional.of(testCountryDto));

        // When & Then
        mockMvc.perform(put("/api/countries/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCountryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Country")))
                .andExpect(jsonPath("$.continent", is("Test Continent")));

        verify(countryApplicationService).update(eq(1L), any(CreateCountryDto.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenCountryDoesNotExist() throws Exception {
        // Given
        when(countryApplicationService.update(eq(999L), any(CreateCountryDto.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/countries/edit/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCountryDto)))
                .andExpect(status().isNotFound());

        verify(countryApplicationService).update(eq(999L), any(CreateCountryDto.class));
    }

    @Test
    void deleteById_ShouldDeleteCountry_WhenCountryExists() throws Exception {
        // Given
        when(countryApplicationService.findById(1L)).thenReturn(Optional.of(testCountryDto));

        // When & Then
        mockMvc.perform(delete("/api/countries/delete/1"))
                .andExpect(status().isNoContent());

        verify(countryApplicationService).findById(1L);
        verify(countryApplicationService).deleteById(1L);
    }

    @Test
    void deleteById_ShouldReturnNotFound_WhenCountryDoesNotExist() throws Exception {
        // Given
        when(countryApplicationService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/countries/delete/999"))
                .andExpect(status().isNotFound());

        verify(countryApplicationService).findById(999L);
        verify(countryApplicationService, never()).deleteById(999L);
    }
}
