package mk.finki.ukim.mk.library.performance;

import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for Book-related API endpoints.
 * Tests the most critical book operations under load to ensure acceptable performance.
 */
@DisplayName("Book Endpoint Performance Tests")
public class BookEndpointPerformanceTest extends BasePerformanceTest {

    @Test
    @DisplayName("Load Test: GET /api/books - Book Catalog Browsing")
    void testGetAllBooksPerformance() throws Exception {
        // This is the most frequently accessed endpoint - users browsing the book catalog
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(get("/api/books"))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 50, 20); // 50 concurrent users, 20 requests each = 1000 total requests

        // Requirements: 95% success rate, average response time under 500ms
        assertPerformanceRequirements(result, 95.0, 500.0);
    }

    @Test
    @DisplayName("Load Test: GET /api/books/{id} - Individual Book Details")
    void testGetBookByIdPerformance() throws Exception {
        // Users frequently access individual book details
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                // Test with different book IDs to simulate real usage
                long bookId = (long) (Math.random() * 10) + 1; // Random ID 1-10
                mockMvc.perform(get("/api/books/" + bookId))
                        .andExpect(isOkOrNotFound()); // Accept 404 for non-existent books
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 30, 25); // 30 concurrent users, 25 requests each = 750 total requests

        // Requirements: 90% success rate (some 404s expected), average response time under 300ms
        assertPerformanceRequirements(result, 90.0, 300.0);
    }

    @Test
    @DisplayName("Load Test: GET /api/books/categories - Category Browsing")
    void testGetCategoriesPerformance() throws Exception {
        // Users browse categories to filter books
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(get("/api/books/categories"))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 40, 15); // 40 concurrent users, 15 requests each = 600 total requests

        // Requirements: 98% success rate, average response time under 200ms (should be fast - enum values)
        assertPerformanceRequirements(result, 98.0, 200.0);
    }

    @Test
    @DisplayName("Load Test: POST /api/books/add - Book Creation (Librarian Operations)")
    void testCreateBookPerformance() throws Exception {
        // Librarians adding new books - less frequent but important for content management
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                CreateBookDto bookDto = new CreateBookDto(
                        "Performance Test Book " + System.currentTimeMillis(),
                        Category.NOVEL,
                        1L, // Assuming author ID 1 exists
                        5
                );

                mockMvc.perform(post("/api/books/add")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                        .andExpect(isOkOrBadRequest()); // Accept some failures due to constraints
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 10, 20); // 10 concurrent librarians, 20 requests each = 200 total requests

        // Requirements: 85% success rate (some failures expected due to constraints), under 1000ms
        assertPerformanceRequirements(result, 85.0, 1000.0);
    }

    @Test
    @DisplayName("Load Test: PUT /api/books/{id}/mark-as-borrowed - Book Borrowing")
    void testMarkAsBorrowedPerformance() throws Exception {
        // Critical transactional operation - users borrowing books
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                long bookId = (long) (Math.random() * 10) + 1; // Random book ID
                mockMvc.perform(put("/api/books/" + bookId + "/mark-as-borrowed")
                        .header("Authorization", "Bearer " + librarianToken))
                        .andExpect(isOkOrNotFound()); // Accept 404 for non-existent books
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 20, 15); // 20 concurrent users, 15 requests each = 300 total requests

        // Requirements: 90% success rate, average response time under 800ms (database transaction)
        assertPerformanceRequirements(result, 90.0, 800.0);
    }

    @Test
    @DisplayName("Load Test: GET /api/books/{id}/history - Book History Retrieval")
    void testGetBookHistoryPerformance() throws Exception {
        // Users checking book change history - potentially complex query
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                long bookId = (long) (Math.random() * 10) + 1; // Random book ID
                mockMvc.perform(get("/api/books/" + bookId + "/history"))
                        .andExpect(isOkOrNotFound());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 25, 12); // 25 concurrent users, 12 requests each = 300 total requests

        // Requirements: 92% success rate, under 600ms (complex query but should be optimized)
        assertPerformanceRequirements(result, 92.0, 600.0);
    }



    @Test
    @DisplayName("Stress Test: Mixed Book Operations")
    void testMixedBookOperationsStress() throws Exception {
        // Simulate real-world mixed usage patterns
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                double random = Math.random();
                
                if (random < 0.4) {
                    // 40% - Browse all books
                    mockMvc.perform(get("/api/books"))
                            .andExpect(status().isOk());
                } else if (random < 0.7) {
                    // 30% - View specific book
                    long bookId = (long) (Math.random() * 10) + 1;
                    mockMvc.perform(get("/api/books/" + bookId))
                            .andExpect(isOkOrNotFound());
                } else if (random < 0.85) {
                    // 15% - Browse categories
                    mockMvc.perform(get("/api/books/categories"))
                            .andExpect(status().isOk());
                } else if (random < 0.95) {
                    // 10% - View book history
                    long bookId = (long) (Math.random() * 10) + 1;
                    mockMvc.perform(get("/api/books/" + bookId + "/history"))
                            .andExpect(isOkOrNotFound());
                } else {
                    // 5% - View books by author
                    mockMvc.perform(get("/api/books/by-author"))
                            .andExpect(status().isOk());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 100, 30); // 100 concurrent users, 30 mixed requests each = 3000 total requests

        // Requirements: 93% success rate, under 400ms average (mixed operations)
        assertPerformanceRequirements(result, 93.0, 400.0);
    }
}
