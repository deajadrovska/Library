package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.repository.CountryRepository;
import mk.finki.ukim.mk.library.service.domain.Impl.CountryServiceImpl;
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
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryServiceImpl countryService;

    private Country testCountry;

    @BeforeEach
    void setUp() {
        testCountry = new Country("Test Country", "Test Continent");
        testCountry.setId(1L);
    }

    @Test
    void findAll_ShouldReturnAllCountries() {
        // Given
        List<Country> expectedCountries = Arrays.asList(testCountry);
        when(countryRepository.findAll()).thenReturn(expectedCountries);

        // When
        List<Country> actualCountries = countryService.findAll();

        // Then
        assertEquals(expectedCountries, actualCountries);
        verify(countryRepository).findAll();
    }

    @Test
    void findById_ShouldReturnCountry_WhenCountryExists() {
        // Given
        when(countryRepository.findById(1L)).thenReturn(Optional.of(testCountry));

        // When
        Optional<Country> result = countryService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCountry, result.get());
        verify(countryRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenCountryDoesNotExist() {
        // Given
        when(countryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Country> result = countryService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(countryRepository).findById(999L);
    }

    @Test
    void save_ShouldReturnSavedCountry() {
        // Given
        when(countryRepository.save(any(Country.class))).thenReturn(testCountry);

        // When
        Optional<Country> result = countryService.save(testCountry);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCountry, result.get());
        verify(countryRepository).save(testCountry);
    }

    @Test
    void update_ShouldReturnUpdatedCountry() {
        // Given
        Country updatedCountry = new Country("Updated Country", "Updated Continent");
        updatedCountry.setId(1L);
        when(countryRepository.save(any(Country.class))).thenReturn(updatedCountry);

        // When
        Optional<Country> result = countryService.update(updatedCountry);

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedCountry, result.get());
        verify(countryRepository).save(updatedCountry);
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // Given
        Long countryId = 1L;

        // When
        countryService.deleteById(countryId);

        // Then
        verify(countryRepository).deleteById(countryId);
    }
}
