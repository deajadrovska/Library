package mk.finki.ukim.mk.library.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.LibraryApplication;
import mk.finki.ukim.mk.library.config.TestSecurityConfig;
import mk.finki.ukim.mk.library.exceptions.InvalidUserCredentialsException;
import mk.finki.ukim.mk.library.exceptions.PasswordsDoNotMatchException;
import mk.finki.ukim.mk.library.model.Dto.*;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.service.application.UserApplicationService;
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

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserApplicationService userApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserDto createUserDto;
    private DisplayUserDto displayUserDto;
    private LoginUserDto loginUserDto;
    private LoginResponseDto loginResponseDto;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserDto(
                "testuser",
                "password",
                "password",
                "Test",
                "User",
                Role.ROLE_USER
        );

        displayUserDto = new DisplayUserDto("testuser", "Test", "User", Role.ROLE_USER);

        loginUserDto = new LoginUserDto("testuser", "password");

        loginResponseDto = new LoginResponseDto("jwt-token-here");
    }

    @Test
    void register_ShouldCreateUser_WhenValidData() throws Exception {
        // Given
        when(userApplicationService.register(any(CreateUserDto.class)))
                .thenReturn(Optional.of(displayUserDto));

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.surname", is("User")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        verify(userApplicationService).register(any(CreateUserDto.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
        // Given
        when(userApplicationService.register(any(CreateUserDto.class)))
                .thenThrow(new PasswordsDoNotMatchException());

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest());

        verify(userApplicationService).register(any(CreateUserDto.class));
    }

    @Test
    void register_ShouldReturnNotFound_WhenRegistrationFails() throws Exception {
        // Given
        when(userApplicationService.register(any(CreateUserDto.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isNotFound());

        verify(userApplicationService).register(any(CreateUserDto.class));
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given
        when(userApplicationService.login(any(LoginUserDto.class)))
                .thenReturn(Optional.of(loginResponseDto));

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is("jwt-token-here")));

        verify(userApplicationService).login(any(LoginUserDto.class));
    }

    @Test
    void login_ShouldReturnNotFound_WhenInvalidCredentials() throws Exception {
        // Given
        when(userApplicationService.login(any(LoginUserDto.class)))
                .thenThrow(new InvalidUserCredentialsException());

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isNotFound());

        verify(userApplicationService).login(any(LoginUserDto.class));
    }

    @Test
    void login_ShouldReturnNotFound_WhenLoginFails() throws Exception {
        // Given
        when(userApplicationService.login(any(LoginUserDto.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isNotFound());

        verify(userApplicationService).login(any(LoginUserDto.class));
    }

    @Test
    void register_ShouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(userApplicationService, never()).register(any(CreateUserDto.class));
    }

    @Test
    void login_ShouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(userApplicationService, never()).login(any(LoginUserDto.class));
    }
}