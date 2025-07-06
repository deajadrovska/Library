package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.model.projections.AuthorNameProjection;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.service.domain.Impl.AuthorServiceImpl;
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
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private Author testAuthor;
    private Country testCountry;

    @BeforeEach
    void setUp() {
        testCountry = new Country("Test Country", "Test Continent");
        testCountry.setId(1L);

        testAuthor = new Author("Test", "Author", testCountry);
        testAuthor.setId(1L);
    }

    @Test
    void findAll_ShouldReturnAllAuthors() {
        // Given
        List<Author> expectedAuthors = Arrays.asList(testAuthor);
        when(authorRepository.findAll()).thenReturn(expectedAuthors);

        // When
        List<Author> actualAuthors = authorService.findAll();

        // Then
        assertEquals(expectedAuthors, actualAuthors);
        verify(authorRepository).findAll();
    }

    @Test
    void findById_ShouldReturnAuthor_WhenAuthorExists() {
        // Given
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));

        // When
        Optional<Author> result = authorService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());
        verify(authorRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenAuthorDoesNotExist() {
        // Given
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Author> result = authorService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(authorRepository).findById(999L);
    }

    @Test
    void save_ShouldReturnSavedAuthor() {
        // Given
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);

        // When
        Optional<Author> result = authorService.save(testAuthor);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAuthor, result.get());
        verify(authorRepository).save(testAuthor);
    }

    @Test
    void update_ShouldReturnUpdatedAuthor() {
        // Given
        Author updatedAuthor = new Author("Updated", "Author", testCountry);
        updatedAuthor.setId(1L);
        when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);

        // When
        Optional<Author> result = authorService.update(updatedAuthor);

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedAuthor, result.get());
        verify(authorRepository).save(updatedAuthor);
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // Given
        Long authorId = 1L;

        // When
        authorService.deleteById(authorId);

        // Then
        verify(authorRepository).deleteById(authorId);
    }

    @Test
    void getAllAuthorNames_ShouldReturnAuthorNameProjections() {
        // Given
        AuthorNameProjection projection1 = mock(AuthorNameProjection.class);
        AuthorNameProjection projection2 = mock(AuthorNameProjection.class);
        List<AuthorNameProjection> expectedProjections = Arrays.asList(projection1, projection2);
        when(authorRepository.findAllProjectedBy()).thenReturn(expectedProjections);

        // When
        List<AuthorNameProjection> result = authorService.getAllAuthorNames();

        // Then
        assertEquals(expectedProjections, result);
        assertEquals(2, result.size());
        verify(authorRepository).findAllProjectedBy();
    }
}
