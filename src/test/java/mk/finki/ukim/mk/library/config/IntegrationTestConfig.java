package mk.finki.ukim.mk.library.config;

import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.security.JwtHelper;
import mk.finki.ukim.mk.library.security.JwtFilter;
import mk.finki.ukim.mk.library.service.domain.UserService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Test configuration for integration tests that need real UserService implementation
 * but still need some mocked security components for JWT testing
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("integration-test")
public class IntegrationTestConfig {

    @Bean
    @Primary
    @Profile("integration-test")
    public JwtHelper testJwtHelper() {
        JwtHelper mockJwtHelper = Mockito.mock(JwtHelper.class);

        // Configure mock behavior for JWT operations to work with any user
        when(mockJwtHelper.generateToken(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return "mock-jwt-token-" + user.getUsername();
        });

        when(mockJwtHelper.extractUsername(anyString())).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);
            // Extract username from token format "mock-jwt-token-{username}"
            if (token.startsWith("mock-jwt-token-")) {
                return token.substring("mock-jwt-token-".length());
            }
            return "testuser"; // fallback
        });

        when(mockJwtHelper.isValid(anyString(), any(User.class))).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);
            User user = invocation.getArgument(1);
            // Check if token matches the user
            return token.equals("mock-jwt-token-" + user.getUsername());
        });

        return mockJwtHelper;
    }

    @Bean
    @Primary
    @Profile("integration-test")
    public JwtFilter testJwtFilter(UserService userService) {
        // Create a test JwtFilter with the test JwtHelper and real UserService
        return new JwtFilter(testJwtHelper(), userService);
    }

    @Bean
    @Primary
    @Profile("integration-test")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/register", "/api/user/login").permitAll()
                        .requestMatchers("/api/books/**", "/api/authors/**", "/api/countries/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Primary
    @Profile("integration-test")
    public UserContext testUserContext() {
        UserContext mockUserContext = Mockito.mock(UserContext.class);

        // Create a test user
        User testUser = new User("testuser", "password", "Test", "User", Role.ROLE_USER);

        // Configure mock behavior
        when(mockUserContext.getCurrentUser()).thenReturn(testUser);
        when(mockUserContext.getCurrentUsername()).thenReturn("testuser");

        return mockUserContext;
    }

    @Bean
    @Primary
    @Profile("integration-test")
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
