package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.Dto.CreateCountryDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.service.application.Impl.CountryApplicationServiceImpl;
import mk.finki.ukim.mk.library.service.domain.CountryService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryApplicationServiceTest {

    @Mock
    private CountryService countryService;

    @InjectMocks
    private CountryApplicationServiceImpl countryApplicationService;

    private Country testCountry;
    private CreateCountryDto createCountryDto;

    @BeforeEach
    void setUp() {
        testCountry = new Country("Test Country", "Test Continent");
        testCountry.setId(1L);

        createCountryDto = new CreateCountryDto("Test Country", "Test Continent");
    }

    @Test
    void findAll_ShouldReturnDisplayCountryDtos() {
        // Given
        List<Country> countries = Arrays.asList(testCountry);
        when(countryService.findAll()).thenReturn(countries);

        // When
        List<DisplayCountryDto> result = countryApplicationService.findAll();

        // Then
        assertEquals(1, result.size());
        DisplayCountryDto dto = result.get(0);
        assertEquals(testCountry.getId(), dto.id());
        assertEquals(testCountry.getName(), dto.name());
        assertEquals(testCountry.getContinent(), dto.continent());
        verify(countryService).findAll();
    }

    @Test
    void findById_ShouldReturnDisplayCountryDto_WhenCountryExists() {
        // Given
        when(countryService.findById(1L)).thenReturn(Optional.of(testCountry));

        // When
        Optional<DisplayCountryDto> result = countryApplicationService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        DisplayCountryDto dto = result.get();
        assertEquals(testCountry.getId(), dto.id());
        assertEquals(testCountry.getName(), dto.name());
        assertEquals(testCountry.getContinent(), dto.continent());
        verify(countryService).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenCountryDoesNotExist() {
        // Given
        when(countryService.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<DisplayCountryDto> result = countryApplicationService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(countryService).findById(999L);
    }

    @Test
    void save_ShouldReturnDisplayCountryDto_WhenCountryIsSaved() {
        // Given
        when(countryService.save(any(Country.class))).thenReturn(Optional.of(testCountry));

        // When
        Optional<DisplayCountryDto> result = countryApplicationService.save(createCountryDto);

        // Then
        assertTrue(result.isPresent());
        DisplayCountryDto dto = result.get();
        assertEquals(testCountry.getId(), dto.id());
        assertEquals(testCountry.getName(), dto.name());
        assertEquals(testCountry.getContinent(), dto.continent());
        verify(countryService).save(any(Country.class));
    }

    @Test
    void save_ShouldReturnEmpty_WhenCountryServiceReturnsEmpty() {
        // Given
        when(countryService.save(any(Country.class))).thenReturn(Optional.empty());

        // When
        Optional<DisplayCountryDto> result = countryApplicationService.save(createCountryDto);

        // Then
        assertFalse(result.isPresent());
        verify(countryService).save(any(Country.class));
    }

    @Test
    void update_ShouldReturnDisplayCountryDto_WhenCountryIsUpdated() {
        // Given
        Country updatedCountry = new Country("Updated Country", "Updated Continent");
        updatedCountry.setId(1L);
        CreateCountryDto updateDto = new CreateCountryDto("Updated Country", "Updated Continent");

        when(countryService.findById(1L)).thenReturn(Optional.of(testCountry));
        when(countryService.update(any(Country.class))).thenReturn(Optional.of(updatedCountry));

        // When
        Optional<DisplayCountryDto> result = countryApplicationService.update(1L, updateDto);

        // Then
        assertTrue(result.isPresent());
        DisplayCountryDto dto = result.get();
        assertEquals(updatedCountry.getId(), dto.id());
        assertEquals("Updated Country", dto.name());
        assertEquals("Updated Continent", dto.continent());
        verify(countryService).findById(1L);
        verify(countryService).update(any(Country.class));
    }

    @Test
    void update_ShouldReturnEmpty_WhenCountryNotFound() {
        // Given
        when(countryService.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<DisplayCountryDto> result = countryApplicationService.update(1L, createCountryDto);

        // Then
        assertFalse(result.isPresent());
        verify(countryService).findById(1L);
        verify(countryService, never()).update(any(Country.class));
    }

    @Test
    void deleteById_ShouldCallCountryServiceDeleteById() {
        // Given
        Long countryId = 1L;

        // When
        countryApplicationService.deleteById(countryId);

        // Then
        verify(countryService).deleteById(countryId);
    }
}
