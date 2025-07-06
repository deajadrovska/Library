package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.Dto.CreateAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthorDto;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.service.application.Impl.AuthorApplicationServiceImpl;
import mk.finki.ukim.mk.library.service.domain.AuthorService;
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
class AuthorApplicationServiceTest {

    @Mock
    private AuthorService authorService;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private AuthorApplicationServiceImpl authorApplicationService;

    private Author testAuthor;
    private Country testCountry;
    private CreateAuthorDto createAuthorDto;

    @BeforeEach
    void setUp() {
        testCountry = new Country("Test Country", "Test Continent");
        testCountry.setId(1L);

        testAuthor = new Author("Test", "Author", testCountry);
        testAuthor.setId(1L);

        createAuthorDto = new CreateAuthorDto("Test", "Author", 1L);
    }

    @Test
    void findAll_ShouldReturnDisplayAuthorDtos() {
        // Given
        List<Author> authors = Arrays.asList(testAuthor);
        when(authorService.findAll()).thenReturn(authors);

        // When
        List<DisplayAuthorDto> result = authorApplicationService.findAll();

        // Then
        assertEquals(1, result.size());
        DisplayAuthorDto dto = result.get(0);
        assertEquals(testAuthor.getId(), dto.id());
        assertEquals(testAuthor.getName(), dto.name());
        assertEquals(testAuthor.getSurname(), dto.surname());
        verify(authorService).findAll();
    }

    @Test
    void findById_ShouldReturnDisplayAuthorDto_WhenAuthorExists() {
        // Given
        when(authorService.findById(1L)).thenReturn(Optional.of(testAuthor));

        // When
        Optional<DisplayAuthorDto> result = authorApplicationService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        DisplayAuthorDto dto = result.get();
        assertEquals(testAuthor.getId(), dto.id());
        assertEquals(testAuthor.getName(), dto.name());
        assertEquals(testAuthor.getSurname(), dto.surname());
        verify(authorService).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenAuthorDoesNotExist() {
        // Given
        when(authorService.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<DisplayAuthorDto> result = authorApplicationService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(authorService).findById(999L);
    }

    @Test
    void save_ShouldReturnDisplayAuthorDto_WhenAuthorIsSaved() {
        // Given
        when(countryService.findById(1L)).thenReturn(Optional.of(testCountry));
        when(authorService.save(any(Author.class))).thenReturn(Optional.of(testAuthor));

        // When
        Optional<DisplayAuthorDto> result = authorApplicationService.save(createAuthorDto);

        // Then
        assertTrue(result.isPresent());
        DisplayAuthorDto dto = result.get();
        assertEquals(testAuthor.getId(), dto.id());
        assertEquals(testAuthor.getName(), dto.name());
        assertEquals(testAuthor.getSurname(), dto.surname());
        verify(countryService).findById(1L);
        verify(authorService).save(any(Author.class));
    }

    @Test
    void save_ShouldReturnEmpty_WhenCountryNotFound() {
        // Given
        when(countryService.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<DisplayAuthorDto> result = authorApplicationService.save(createAuthorDto);

        // Then
        assertFalse(result.isPresent());
        verify(countryService).findById(1L);
        verify(authorService, never()).save(any(Author.class));
    }

    @Test
    void update_ShouldReturnDisplayAuthorDto_WhenAuthorIsUpdated() {
        // Given
        Author updatedAuthor = new Author("Updated", "Author", testCountry);
        updatedAuthor.setId(1L);
        CreateAuthorDto updateDto = new CreateAuthorDto("Updated", "Author", 1L);

        when(authorService.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(countryService.findById(1L)).thenReturn(Optional.of(testCountry));
        when(authorService.update(any(Author.class))).thenReturn(Optional.of(updatedAuthor));

        // When
        Optional<DisplayAuthorDto> result = authorApplicationService.update(1L, updateDto);

        // Then
        assertTrue(result.isPresent());
        DisplayAuthorDto dto = result.get();
        assertEquals(updatedAuthor.getId(), dto.id());
        assertEquals("Updated", dto.name());
        assertEquals("Author", dto.surname());
        verify(authorService).findById(1L);
        verify(countryService).findById(1L);
        verify(authorService).update(any(Author.class));
    }

    @Test
    void update_ShouldReturnEmpty_WhenAuthorNotFound() {
        // Given
        when(authorService.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<DisplayAuthorDto> result = authorApplicationService.update(1L, createAuthorDto);

        // Then
        assertFalse(result.isPresent());
        verify(authorService).findById(1L);
        verify(countryService, never()).findById(any());
        verify(authorService, never()).update(any(Author.class));
    }

    @Test
    void update_ShouldReturnEmpty_WhenCountryNotFound() {
        // Given
        when(authorService.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(countryService.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<DisplayAuthorDto> result = authorApplicationService.update(1L, createAuthorDto);

        // Then
        assertFalse(result.isPresent());
        verify(authorService).findById(1L);
        verify(countryService).findById(1L);
        verify(authorService, never()).update(any(Author.class));
    }

    @Test
    void deleteById_ShouldCallAuthorServiceDeleteById() {
        // Given
        Long authorId = 1L;

        // When
        authorApplicationService.deleteById(authorId);

        // Then
        verify(authorService).deleteById(authorId);
    }
}
