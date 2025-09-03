package mk.finki.ukim.mk.library.performance;

import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * System-wide performance tests that simulate real-world usage patterns
 * across all API endpoints to test overall system performance and stability.
 */
@DisplayName("System-Wide Performance Tests")
public class SystemWidePerformanceTest extends BasePerformanceTest {

    @Test
    @DisplayName("End-to-End Performance Test: Complete User Journey")
    void testCompleteUserJourneyPerformance() throws Exception {
        // Simulate a complete user journey from login to book borrowing
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                // Step 1: Login (10% of operations)
                if (Math.random() < 0.1) {
                    LoginUserDto loginDto = new LoginUserDto(uniqueUsername, "password123");
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                            .andExpect(status().isOk());
                    return true;
                }
                
                // Step 2: Browse books (30% of operations)
                if (Math.random() < 0.4) {
                    mockMvc.perform(get("/api/books"))
                            .andExpect(status().isOk());
                    return true;
                }
                
                // Step 3: View book details (25% of operations)
                if (Math.random() < 0.65) {
                    long bookId = (long) (Math.random() * 10) + 1;
                    mockMvc.perform(get("/api/books/" + bookId))
                            .andExpect(isOkOrNotFound());
                    return true;
                }
                
                // Step 4: Browse authors (15% of operations)
                if (Math.random() < 0.8) {
                    mockMvc.perform(get("/api/authors"))
                            .andExpect(status().isOk());
                    return true;
                }
                
                // Step 5: Wishlist operations (15% of operations)
                if (Math.random() < 0.9) {
                    mockMvc.perform(get("/api/wishlist")
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(isOkOrNotFound());
                    return true;
                }
                
                // Step 6: Add to wishlist (10% of operations)
                long bookId = (long) (Math.random() * 10) + 1;
                mockMvc.perform(post("/api/wishlist/add/" + bookId)
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(isOkOrBadRequest());
                return true;
                
            } catch (Exception e) {
                return false;
            }
        }, 150, 30); // 150 concurrent users, 30 operations each = 4500 total requests

        // Requirements: 90% success rate, under 500ms average (complete user journey)
        assertPerformanceRequirements(result, 90.0, 500.0);
    }

    @Test
    @DisplayName("Peak Traffic Simulation: Black Friday Scenario")
    void testBlackFridayTrafficSimulation() throws Exception {
        // Simulate extreme peak traffic like Black Friday or book sale events
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                double random = Math.random();
                
                // Heavy browsing pattern during sales
                if (random < 0.35) {
                    // 35% - Browse all books
                    mockMvc.perform(get("/api/books"))
                            .andExpect(status().isOk());
                } else if (random < 0.6) {
                    // 25% - View specific books
                    long bookId = (long) (Math.random() * 20) + 1;
                    mockMvc.perform(get("/api/books/" + bookId))
                            .andExpect(isOkOrNotFound());
                } else if (random < 0.8) {
                    // 20% - Add to wishlist (high during sales)
                    long bookId = (long) (Math.random() * 10) + 1;
                    mockMvc.perform(post("/api/wishlist/add/" + bookId)
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(isOkOrBadRequest());
                } else if (random < 0.9) {
                    // 10% - Check wishlist
                    mockMvc.perform(get("/api/wishlist")
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(isOkOrNotFound());
                } else {
                    // 10% - Browse categories
                    mockMvc.perform(get("/api/books/categories"))
                            .andExpect(status().isOk());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 300, 20); // 300 concurrent users, 20 operations each = 6000 total requests

        // Requirements: 85% success rate (system under extreme stress), under 800ms
        assertPerformanceRequirements(result, 85.0, 800.0);
    }

    @Test
    @DisplayName("Mixed User Types Performance: Users vs Librarians")
    void testMixedUserTypesPerformance() throws Exception {
        // Test performance with realistic mix of regular users and librarians
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                boolean isLibrarian = Math.random() < 0.1; // 10% librarians, 90% users
                String token = isLibrarian ? librarianToken : userToken;
                
                if (isLibrarian) {
                    // Librarian operations
                    double operation = Math.random();
                    if (operation < 0.4) {
                        // Create book
                        CreateBookDto bookDto = new CreateBookDto(
                                "Librarian Book " + System.currentTimeMillis(),
                                Category.NOVEL,
                                1L,
                                5
                        );
                        mockMvc.perform(post("/api/books/add")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bookDto)))
                                .andExpect(isOkOrBadRequest());
                    } else if (operation < 0.7) {
                        // Mark book as borrowed
                        long bookId = (long) (Math.random() * 10) + 1;
                        mockMvc.perform(put("/api/books/" + bookId + "/mark-as-borrowed")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(isOkOrNotFound());
                    } else {
                        // Browse books (librarians also browse)
                        mockMvc.perform(get("/api/books"))
                                .andExpect(status().isOk());
                    }
                } else {
                    // Regular user operations
                    double operation = Math.random();
                    if (operation < 0.4) {
                        // Browse books
                        mockMvc.perform(get("/api/books"))
                                .andExpect(status().isOk());
                    } else if (operation < 0.6) {
                        // View book details
                        long bookId = (long) (Math.random() * 10) + 1;
                        mockMvc.perform(get("/api/books/" + bookId))
                                .andExpect(isOkOrNotFound());
                    } else if (operation < 0.8) {
                        // Wishlist operations
                        mockMvc.perform(get("/api/wishlist")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(isOkOrNotFound());
                    } else {
                        // Add to wishlist
                        long bookId = (long) (Math.random() * 10) + 1;
                        mockMvc.perform(post("/api/wishlist/add/" + bookId)
                                .header("Authorization", "Bearer " + token))
                                .andExpect(isOkOrBadRequest());
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 120, 25); // 120 concurrent users (mix of types), 25 operations each = 3000 total requests

        // Requirements: 88% success rate, under 600ms
        assertPerformanceRequirements(result, 88.0, 600.0);
    }

    @Test
    @DisplayName("Database Intensive Operations Performance")
    void testDatabaseIntensiveOperationsPerformance() throws Exception {
        // Test operations that are database-intensive
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                double random = Math.random();
                
                if (random < 0.3) {
                    // 30% - Book history (complex query)
                    long bookId = (long) (Math.random() * 10) + 1;
                    mockMvc.perform(get("/api/books/" + bookId + "/history"))
                            .andExpect(isOkOrNotFound());
                } else if (random < 0.5) {
                    // 20% - Books by author view (materialized view)
                    mockMvc.perform(get("/api/books/by-author"))
                            .andExpect(status().isOk());
                } else if (random < 0.7) {
                    // 20% - Authors by country view (materialized view)
                    mockMvc.perform(get("/api/authors/by-country"))
                            .andExpect(status().isOk());
                } else if (random < 0.85) {
                    // 15% - Borrow all books (complex transaction)
                    mockMvc.perform(post("/api/wishlist/borrow")
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(isOkOrBadRequest());
                } else {
                    // 15% - Author names projection
                    mockMvc.perform(get("/api/authors/names"))
                            .andExpect(status().isOk());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 80, 20); // 80 concurrent users, 20 database operations each = 1600 total requests

        // Requirements: 87% success rate, under 700ms (database operations are slower)
        assertPerformanceRequirements(result, 87.0, 700.0);
    }

    @Test
    @DisplayName("Sustained Load Test: 24/7 Operation Simulation")
    void testSustainedLoadPerformance() throws Exception {
        // Test system stability under sustained load over time
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                // Realistic 24/7 usage pattern
                double random = Math.random();
                
                if (random < 0.25) {
                    // 25% - Book browsing
                    mockMvc.perform(get("/api/books"))
                            .andExpect(status().isOk());
                } else if (random < 0.4) {
                    // 15% - Author browsing
                    mockMvc.perform(get("/api/authors"))
                            .andExpect(status().isOk());
                } else if (random < 0.55) {
                    // 15% - Book details
                    long bookId = (long) (Math.random() * 10) + 1;
                    mockMvc.perform(get("/api/books/" + bookId))
                            .andExpect(isOkOrNotFound());
                } else if (random < 0.7) {
                    // 15% - Wishlist viewing
                    mockMvc.perform(get("/api/wishlist")
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(isOkOrNotFound());
                } else if (random < 0.8) {
                    // 10% - Login operations
                    LoginUserDto loginDto = new LoginUserDto(uniqueUsername, "password123");
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                            .andExpect(status().isOk());
                } else if (random < 0.9) {
                    // 10% - Add to wishlist
                    long bookId = (long) (Math.random() * 10) + 1;
                    mockMvc.perform(post("/api/wishlist/add/" + bookId)
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(isOkOrBadRequest());
                } else {
                    // 10% - Categories and other light operations
                    mockMvc.perform(get("/api/books/categories"))
                            .andExpect(status().isOk());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 100, 50); // 100 concurrent users, 50 sustained operations each = 5000 total requests

        // Requirements: 92% success rate, under 400ms (should maintain performance over time)
        assertPerformanceRequirements(result, 92.0, 400.0);
    }

    @Test
    @DisplayName("Error Handling Performance: System Resilience")
    void testErrorHandlingPerformance() throws Exception {
        // Test system performance when handling various error conditions
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                double random = Math.random();
                
                if (random < 0.3) {
                    // 30% - Non-existent book requests
                    long invalidBookId = (long) (Math.random() * 1000) + 100;
                    mockMvc.perform(get("/api/books/" + invalidBookId))
                            .andExpect(status().isNotFound());
                } else if (random < 0.5) {
                    // 20% - Invalid login attempts
                    LoginUserDto invalidLogin = new LoginUserDto("invaliduser", "wrongpass");
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidLogin)))
                            .andExpect(status().isNotFound());
                } else if (random < 0.7) {
                    // 20% - Unauthorized wishlist access (returns 403 Forbidden, not 401 Unauthorized)
                    mockMvc.perform(get("/api/wishlist"))
                            .andExpect(status().isForbidden());
                } else if (random < 0.85) {
                    // 15% - Invalid book additions to wishlist
                    long invalidBookId = (long) (Math.random() * 1000) + 100;
                    mockMvc.perform(post("/api/wishlist/add/" + invalidBookId)
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(status().isBadRequest());
                } else {
                    // 15% - Malformed requests
                    mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json"))
                            .andExpect(status().isBadRequest());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 90, 30); // 90 concurrent error scenarios, 30 each = 2700 total requests

        // Requirements: 95% success rate (errors should be handled gracefully), under 250ms
        assertPerformanceRequirements(result, 95.0, 250.0);
    }
}
