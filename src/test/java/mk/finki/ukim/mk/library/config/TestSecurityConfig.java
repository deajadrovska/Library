package mk.finki.ukim.mk.library.config;

import mk.finki.ukim.mk.library.config.CustomUsernamePasswordAuthenticationProvider;
import mk.finki.ukim.mk.library.config.UserContext;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.mockito.Mockito.when;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    @Profile({"test", "controller-test"})  // Only for controller tests, not integration tests
    public UserService testUserService() {
        UserService mockUserService = Mockito.mock(UserService.class);

        // Create a test user for JWT validation
        User testUser = new User("testuser", "password", "Test", "User", Role.ROLE_USER);

        // Configure mock behavior for JWT filter
        when(mockUserService.findByUsername("testuser")).thenReturn(testUser);

        return mockUserService;
    }

    @Bean
    @Primary
    @Profile("test")
    public JwtFilter testJwtFilter() {
        // Create a test JwtFilter with the test JwtHelper and mock UserService
        return new JwtFilter(testJwtHelper(), testUserService());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3001"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                                                       CustomUsernamePasswordAuthenticationProvider authenticationProvider,
                                                       JwtFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF like production
                .cors(corsCustomizer ->
                        corsCustomizer.configurationSource(corsConfigurationSource())
                )
                .authorizeHttpRequests(authorizeHttpRequestsCustomizer ->
                        authorizeHttpRequestsCustomizer
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/api/user/register",
                                        "/api/user/login"
                                )
                                .permitAll()
                                .requestMatchers(
                                        "/api/countries/**",
                                        "/api/authors/**",
                                        "/api/books/**"
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Primary
    @Profile("test")
    public JwtHelper testJwtHelper() {
        // Return a real JwtHelper instance for test profile
        return new mk.finki.ukim.mk.library.security.JwtHelper();
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



    @Bean
    @Primary
    @Profile("test")
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}