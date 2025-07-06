package mk.finki.ukim.mk.library.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;
import mk.finki.ukim.mk.library.model.projections.AuthorNameProjection;
import mk.finki.ukim.mk.library.model.views.AuthorsByCountryView;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.repository.AuthorsByCountryViewRepository;
import mk.finki.ukim.mk.library.service.application.AuthorApplicationService;
import mk.finki.ukim.mk.library.service.domain.AuthorService;
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

@WebMvcTest(controllers = AuthorController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test")
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorApplicationService authorApplicationService;

    @MockBean
    private AuthorsByCountryViewRepository authorsByCountryViewRepository;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private AuthorService authorDomainService;

    @Autowired
    private ObjectMapper objectMapper;

    private DisplayAuthorDto testAuthorDto;
    private CreateAuthorDto createAuthorDto;
    private DisplayCountryDto testCountryDto;

    @BeforeEach
    void setUp() {
        testCountryDto = new DisplayCountryDto(1L, "Test Country", "Test Continent");
        testAuthorDto = new DisplayAuthorDto(1L, "Test", "Author", testCountryDto);
        createAuthorDto = new CreateAuthorDto("Test", "Author", 1L);
    }

    @Test
    void findAll_ShouldReturnAllAuthors() throws Exception {
        // Given
        List<DisplayAuthorDto> authors = Arrays.asList(testAuthorDto);
        when(authorApplicationService.findAll()).thenReturn(authors);

        // When & Then
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test")))
                .andExpect(jsonPath("$[0].surname", is("Author")))
                .andExpect(jsonPath("$[0].country.id", is(1)))
                .andExpect(jsonPath("$[0].country.name", is("Test Country")));

        verify(authorApplicationService).findAll();
    }

    @Test
    void findById_ShouldReturnAuthor_WhenAuthorExists() throws Exception {
        // Given
        when(authorApplicationService.findById(1L)).thenReturn(Optional.of(testAuthorDto));

        // When & Then
        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.surname", is("Author")));

        verify(authorApplicationService).findById(1L);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenAuthorDoesNotExist() throws Exception {
        // Given
        when(authorApplicationService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/authors/999"))
                .andExpect(status().isNotFound());

        verify(authorApplicationService).findById(999L);
    }

    @Test
    void save_ShouldCreateAuthor_WhenValidData() throws Exception {
        // Given
        when(authorApplicationService.save(any(CreateAuthorDto.class))).thenReturn(Optional.of(testAuthorDto));

        // When & Then
        mockMvc.perform(post("/api/authors/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.surname", is("Author")));

        verify(authorApplicationService).save(any(CreateAuthorDto.class));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        when(authorApplicationService.save(any(CreateAuthorDto.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/authors/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isBadRequest());

        verify(authorApplicationService).save(any(CreateAuthorDto.class));
    }

    @Test
    void update_ShouldUpdateAuthor_WhenValidData() throws Exception {
        // Given
        when(authorApplicationService.update(eq(1L), any(CreateAuthorDto.class)))
                .thenReturn(Optional.of(testAuthorDto));

        // When & Then
        mockMvc.perform(put("/api/authors/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.surname", is("Author")));

        verify(authorApplicationService).update(eq(1L), any(CreateAuthorDto.class));
    }

    @Test
    void update_ShouldReturnNotFound_WhenAuthorDoesNotExist() throws Exception {
        // Given
        when(authorApplicationService.update(eq(999L), any(CreateAuthorDto.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/authors/edit/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isNotFound());

        verify(authorApplicationService).update(eq(999L), any(CreateAuthorDto.class));
    }

    @Test
    void deleteById_ShouldDeleteAuthor_WhenAuthorExists() throws Exception {
        // Given
        when(authorApplicationService.findById(1L)).thenReturn(Optional.of(testAuthorDto));

        // When & Then
        mockMvc.perform(delete("/api/authors/delete/1"))
                .andExpect(status().isNoContent());

        verify(authorApplicationService).findById(1L);
        verify(authorApplicationService).deleteById(1L);
    }

    @Test
    void deleteById_ShouldReturnNotFound_WhenAuthorDoesNotExist() throws Exception {
        // Given
        when(authorApplicationService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/authors/delete/999"))
                .andExpect(status().isNotFound());

        verify(authorApplicationService).findById(999L);
        verify(authorApplicationService, never()).deleteById(999L);
    }

    @Test
    void getAuthorsCountByCountry_ShouldReturnAuthorsByCountryView() throws Exception {
        // Given
        List<AuthorsByCountryView> views = Arrays.asList();
        when(authorsByCountryViewRepository.findAll()).thenReturn(views);

        // When & Then
        mockMvc.perform(get("/api/authors/by-country"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authorsByCountryViewRepository).findAll();
    }

    @Test
    void getAuthorNames_ShouldReturnAuthorNameProjections() throws Exception {
        // Given
        List<AuthorNameProjection> projections = Arrays.asList();
        when(authorDomainService.getAllAuthorNames()).thenReturn(projections);

        // When & Then
        mockMvc.perform(get("/api/authors/names"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authorDomainService).getAllAuthorNames();
    }
}
