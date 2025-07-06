package mk.finki.ukim.mk.library.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.repository.BooksByAuthorViewRepository;
import mk.finki.ukim.mk.library.service.application.BookApplicationService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookApplicationService bookApplicationService;

    @MockBean
    private BooksByAuthorViewRepository booksByAuthorViewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private DisplayBookDto testBookDto;
    private CreateBookDto createBookDto;

    @BeforeEach
    void setUp() {
        DisplayCountryDto countryDto = new DisplayCountryDto(1L, "Test Country", "Test Continent");
        DisplayAuthorDto authorDto = new DisplayAuthorDto(1L, "Test", "Author", countryDto);

        testBookDto = new DisplayBookDto(1L, "Test Book", Category.NOVEL, authorDto, 5);
        createBookDto = new CreateBookDto("Test Book", Category.NOVEL, 1L, 5);
    }

    @Test
    void findAll_ShouldReturnAllBooks() throws Exception {
        // Given
        List<DisplayBookDto> books = Arrays.asList(testBookDto);
        when(bookApplicationService.findAll()).thenReturn(books);

        // When & Then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Book")))
                .andExpect(jsonPath("$[0].category", is("NOVEL")))
                .andExpect(jsonPath("$[0].availableCopies", is(5)));

        verify(bookApplicationService).findAll();
    }

    @Test
    void findById_ShouldReturnBook_WhenBookExists() throws Exception {
        // Given
        when(bookApplicationService.findById(1L)).thenReturn(Optional.of(testBookDto));

        // When & Then
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Book")))
                .andExpect(jsonPath("$.category", is("NOVEL")));

        verify(bookApplicationService).findById(1L);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // Given
        when(bookApplicationService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());

        verify(bookApplicationService).findById(999L);
    }

    @Test
    void save_ShouldCreateBook_WhenValidData() throws Exception {
        // Given
        when(bookApplicationService.save(any(CreateBookDto.class))).thenReturn(Optional.of(testBookDto));

        // When & Then
        mockMvc.perform(post("/api/books/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Book")))
                .andExpect(jsonPath("$.category", is("NOVEL")));

        verify(bookApplicationService).save(any(CreateBookDto.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        when(bookApplicationService.save(any(CreateBookDto.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/books/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookDto)))
                .andExpect(status().isBadRequest());

        verify(bookApplicationService).save(any(CreateBookDto.class));
    }

    @Test
    void update_ShouldUpdateBook_WhenValidData() throws Exception {
        // Given
        when(bookApplicationService.update(eq(1L), any(CreateBookDto.class)))
                .thenReturn(Optional.of(testBookDto));

        // When & Then
        mockMvc.perform(put("/api/books/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Book")));

        verify(bookApplicationService).update(eq(1L), any(CreateBookDto.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // Given
        when(bookApplicationService.update(eq(999L), any(CreateBookDto.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/books/edit/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookDto)))
                .andExpect(status().isNotFound());

        verify(bookApplicationService).update(eq(999L), any(CreateBookDto.class));
    }

    @Test
    void deleteById_ShouldDeleteBook_WhenBookExists() throws Exception {
        // Given
        when(bookApplicationService.findById(1L)).thenReturn(Optional.of(testBookDto));
        doNothing().when(bookApplicationService).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/books/delete/1"))
                .andExpect(status().isNoContent());

        verify(bookApplicationService).findById(1L);
        verify(bookApplicationService).deleteById(1L);
    }

    @Test
    void deleteById_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // Given
        when(bookApplicationService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/books/delete/999"))
                .andExpect(status().isNotFound());

        verify(bookApplicationService).findById(999L);
        verify(bookApplicationService, never()).deleteById(999L);
    }

    @Test
    void markAsBorrowed_ShouldMarkBookAsBorrowed_WhenBookExists() throws Exception {
        // Given
        DisplayBookDto borrowedBook = new DisplayBookDto(1L, "Test Book", Category.NOVEL,
                testBookDto.author(), 4); // One less copy
        when(bookApplicationService.markAsBorrowed(1L)).thenReturn(Optional.of(borrowedBook));

        // When & Then
        mockMvc.perform(put("/api/books/1/mark-as-borrowed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.availableCopies", is(4)));

        verify(bookApplicationService).markAsBorrowed(1L);
    }

    @Test
    void findAllCategories_ShouldReturnAllCategories() throws Exception {
        // Given
        List<Category> categories = Arrays.asList(Category.values());
        when(bookApplicationService.findAllCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/books/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(categories.size())))
                .andExpect(jsonPath("$", hasItem("NOVEL")))
                .andExpect(jsonPath("$", hasItem("FANTASY")))
                .andExpect(jsonPath("$", hasItem("HISTORY")));

        verify(bookApplicationService).findAllCategories();
    }

    @Test
    void getBookHistory_ShouldReturnHistory_WhenBookExists() throws Exception {
        // Given
        when(bookApplicationService.findById(1L)).thenReturn(Optional.of(testBookDto));
        when(bookApplicationService.getBookHistory(1L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/books/1/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookApplicationService).findById(1L);
        verify(bookApplicationService).getBookHistory(1L);
    }

    @Test
    void getBookHistory_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // Given
        when(bookApplicationService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/books/999/history"))
                .andExpect(status().isNotFound());

        verify(bookApplicationService).findById(999L);
        verify(bookApplicationService, never()).getBookHistory(999L);
    }
}