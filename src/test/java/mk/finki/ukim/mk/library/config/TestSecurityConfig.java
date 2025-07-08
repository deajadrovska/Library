package mk.finki.ukim.mk.library.config;

import mk.finki.ukim.mk.library.config.CustomUsernamePasswordAuthenticationProvider;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import mk.finki.ukim.mk.library.security.JwtHelper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/wishlist/**").authenticated()
                        .anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                ))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .securityMatcher("/**"); // Ensure this filter chain matches all requests
        return http.build();
    }

    @Bean
    @Primary
    @Profile("test")
    public JwtHelper testJwtHelper() {
        JwtHelper mockJwtHelper = Mockito.mock(JwtHelper.class);

        // Configure mock behavior
        when(mockJwtHelper.extractUsername(anyString())).thenReturn("testuser");
        when(mockJwtHelper.extractExpiration(anyString())).thenReturn(new Date(System.currentTimeMillis() + 86400000));
        when(mockJwtHelper.generateToken(any(UserDetails.class))).thenAnswer(invocation -> {
            UserDetails userDetails = invocation.getArgument(0);
            return "test-jwt-token-" + userDetails.getUsername();
        });
        when(mockJwtHelper.isValid(anyString(), any(UserDetails.class))).thenReturn(true);

        return mockJwtHelper;
    }

    @Bean
    @Primary
    @Profile("test")
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
    @Profile("test")
    public AuthenticationProvider testAuthenticationProvider() {
        return Mockito.mock(CustomUsernamePasswordAuthenticationProvider.class);
    }
}