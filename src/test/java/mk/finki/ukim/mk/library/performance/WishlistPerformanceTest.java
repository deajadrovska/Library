package mk.finki.ukim.mk.library.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for Wishlist-related API endpoints.
 * These endpoints are user-specific and require authentication, testing personalized operations.
 */
@DisplayName("Wishlist Performance Tests")
public class WishlistPerformanceTest extends BasePerformanceTest {

    @Test
    @DisplayName("Load Test: GET /api/wishlist - User Wishlist Retrieval")
    void testGetWishlistPerformance() throws Exception {
        // Users frequently check their wishlist
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(isOkOrNotFound()); // 404 if no wishlist exists
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 60, 15); // 60 concurrent users, 15 requests each = 900 total requests

        // Requirements: 95% success rate, under 400ms (user-specific query)
        assertPerformanceRequirements(result, 95.0, 400.0);
    }

    @Test
    @DisplayName("Load Test: POST /api/wishlist/add/{id} - Add Book to Wishlist")
    void testAddBookToWishlistPerformance() throws Exception {
        // Critical user operation - adding books to wishlist
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                long bookId = (long) (Math.random() * 10) + 1; // Random book ID 1-10
                mockMvc.perform(post("/api/wishlist/add/" + bookId)
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(isOkOrBadRequest()); // May fail if book unavailable
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 1, 100); // 1 user, 100 requests = 100 total requests (sequential to avoid constraint violations)

        // Requirements: 90% success rate (some failures expected due to constraints), under 600ms
        assertPerformanceRequirements(result, 90.0, 600.0);
    }

    @Test
    @DisplayName("Load Test: GET /api/wishlist/books - List Books in Wishlist")
    void testListWishlistBooksPerformance() throws Exception {
        // Users viewing books in their wishlist
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(get("/api/wishlist/books")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 50, 12); // 50 concurrent users, 12 requests each = 600 total requests

        // Requirements: 96% success rate, under 350ms
        assertPerformanceRequirements(result, 96.0, 350.0);
    }

    @Test
    @DisplayName("Load Test: POST /api/wishlist/borrow - Borrow All Books in Wishlist")
    void testBorrowAllBooksPerformance() throws Exception {
        // Critical transactional operation - borrowing all books from wishlist
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(post("/api/wishlist/borrow")
                        .header("Authorization", "Bearer " + userToken))
                        .andExpect(isOkOrBadRequest()); // May fail if insufficient copies
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 25, 10); // 25 concurrent users, 10 requests each = 250 total requests

        // Requirements: 80% success rate (many failures expected), under 1000ms (complex transaction)
        assertPerformanceRequirements(result, 80.0, 1000.0);
    }



    @Test
    @DisplayName("Authentication Load Test: Multiple Users with Different Tokens")
    void testMultipleUserWishlistAccess() throws Exception {
        // Test performance with different authenticated users
        // This simulates real-world scenario where different users access their wishlists
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                // Alternate between user and librarian tokens
                String token = Math.random() < 0.8 ? userToken : librarianToken;
                
                double operation = Math.random();
                if (operation < 0.5) {
                    // 50% - Get wishlist
                    mockMvc.perform(get("/api/wishlist")
                            .header("Authorization", "Bearer " + token))
                            .andExpect(isOkOrNotFound());
                } else {
                    // 50% - List books in wishlist
                    mockMvc.perform(get("/api/wishlist/books")
                            .header("Authorization", "Bearer " + token))
                            .andExpect(status().isOk());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 90, 20); // 90 concurrent users, 20 requests each = 1800 total requests

        // Requirements: 94% success rate, under 400ms
        assertPerformanceRequirements(result, 94.0, 400.0);
    }

    @Test
    @DisplayName("Peak Load Test: High-Frequency Wishlist Access")
    void testPeakWishlistLoad() throws Exception {
        // Test system under peak load conditions (e.g., during promotions)
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                // Focus on read operations during peak times
                if (Math.random() < 0.7) {
                    mockMvc.perform(get("/api/wishlist")
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(isOkOrNotFound());
                } else {
                    mockMvc.perform(get("/api/wishlist/books")
                            .header("Authorization", "Bearer " + userToken))
                            .andExpect(status().isOk());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 150, 10); // 150 concurrent users, 10 quick requests each = 1500 total requests

        // Requirements: 92% success rate, under 450ms (system should handle peak load)
        assertPerformanceRequirements(result, 92.0, 450.0);
    }
}
