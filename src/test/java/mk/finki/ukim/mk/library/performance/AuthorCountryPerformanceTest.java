package mk.finki.ukim.mk.library.performance;

import mk.finki.ukim.mk.library.model.Dto.CreateAuthorDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for Author and Country management endpoints.
 * These endpoints support browsing and content management operations.
 */
@DisplayName("Author and Country Performance Tests")
public class AuthorCountryPerformanceTest extends BasePerformanceTest {

    @Test
    @DisplayName("Load Test: GET /api/authors - Author Browsing")
    void testGetAllAuthorsPerformance() throws Exception {
        // Users browsing authors to find books
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(get("/api/authors"))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 70, 15); // 70 concurrent users, 15 requests each = 1050 total requests

        // Requirements: 97% success rate, under 300ms
        assertPerformanceRequirements(result, 97.0, 300.0);
    }

    @Test
    @DisplayName("Load Test: GET /api/authors/{id} - Individual Author Details")
    void testGetAuthorByIdPerformance() throws Exception {
        // Users viewing specific author information
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                long authorId = (long) (Math.random() * 10) + 1; // Random author ID 1-10
                mockMvc.perform(get("/api/authors/" + authorId))
                        .andExpect(isOkOrNotFound());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 50, 20); // 50 concurrent users, 20 requests each = 1000 total requests

        // Requirements: 92% success rate (some 404s expected), under 250ms
        assertPerformanceRequirements(result, 92.0, 250.0);
    }

    @Test
    @DisplayName("Load Test: GET /api/countries - Country Browsing")
    void testGetAllCountriesPerformance() throws Exception {
        // Users browsing countries (for filtering authors/books)
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(get("/api/countries"))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 60, 12); // 60 concurrent users, 12 requests each = 720 total requests

        // Requirements: 99% success rate, under 150ms (should be very fast - small dataset)
        assertPerformanceRequirements(result, 99.0, 150.0);
    }

    @Test
    @DisplayName("Load Test: GET /api/authors/names - Author Names Projection")
    void testGetAuthorNamesPerformance() throws Exception {
        // Optimized query for author names only - should be very fast
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                mockMvc.perform(get("/api/authors/names"))
                        .andExpect(status().isOk());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 80, 10); // 80 concurrent users, 10 requests each = 800 total requests

        // Requirements: 98% success rate, under 200ms (projection query should be fast)
        assertPerformanceRequirements(result, 98.0, 200.0);
    }



    @Test
    @DisplayName("Load Test: POST /api/authors/add - Author Creation")
    void testCreateAuthorPerformance() throws Exception {
        // Librarians adding new authors - content management operation
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                CreateAuthorDto authorDto = new CreateAuthorDto(
                        "Performance",
                        "Author " + System.currentTimeMillis(),
                        1L // Assuming country ID 1 exists
                );
                
                mockMvc.perform(post("/api/authors/add")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDto)))
                        .andExpect(isOkOrBadRequest());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 15, 10); // 15 concurrent librarians, 10 requests each = 150 total requests

        // Requirements: 90% success rate, under 600ms
        assertPerformanceRequirements(result, 90.0, 600.0);
    }

    @Test
    @DisplayName("Load Test: PUT /api/authors/edit/{id} - Author Updates")
    void testUpdateAuthorPerformance() throws Exception {
        // Librarians updating author information
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                long authorId = (long) (Math.random() * 10) + 1; // Random author ID
                CreateAuthorDto authorDto = new CreateAuthorDto(
                        "Updated",
                        "Author " + System.currentTimeMillis(),
                        1L
                );
                
                mockMvc.perform(put("/api/authors/edit/" + authorId)
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorDto)))
                        .andExpect(isOkOrNotFound());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 12, 8); // 12 concurrent librarians, 8 requests each = 96 total requests

        // Requirements: 88% success rate, under 500ms
        assertPerformanceRequirements(result, 88.0, 500.0);
    }

    @Test
    @DisplayName("Load Test: DELETE /api/authors/delete/{id} - Author Deletion")
    void testDeleteAuthorPerformance() throws Exception {
        // Librarians deleting authors - should be infrequent but tested
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                long authorId = (long) (Math.random() * 100) + 50; // Higher IDs to avoid deleting test data
                
                mockMvc.perform(delete("/api/authors/delete/" + authorId)
                        .header("Authorization", "Bearer " + librarianToken))
                        .andExpect(isNoContentOrNotFound());
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 8, 5); // 8 concurrent librarians, 5 requests each = 40 total requests

        // Requirements: 85% success rate (many 404s expected), under 400ms
        assertPerformanceRequirements(result, 85.0, 400.0);
    }


    @Test
    @DisplayName("Content Management Load Test: Librarian Operations")
    void testLibrarianContentManagementLoad() throws Exception {
        // Test content management operations under concurrent librarian usage
        
        PerformanceResult result = executeLoadTest(() -> {
            try {
                double random = Math.random();
                
                if (random < 0.6) {
                    // 60% - Create new author
                    CreateAuthorDto authorDto = new CreateAuthorDto(
                            "Librarian",
                            "Test " + System.currentTimeMillis(),
                            1L
                    );
                    mockMvc.perform(post("/api/authors/add")
                            .header("Authorization", "Bearer " + librarianToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authorDto)))
                            .andExpect(isOkOrBadRequest());
                } else {
                    // 40% - Update existing author
                    long authorId = (long) (Math.random() * 10) + 1;
                    CreateAuthorDto authorDto = new CreateAuthorDto(
                            "Updated",
                            "Librarian " + System.currentTimeMillis(),
                            1L
                    );
                    mockMvc.perform(put("/api/authors/edit/" + authorId)
                            .header("Authorization", "Bearer " + librarianToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authorDto)))
                            .andExpect(isOkOrNotFound());
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }, 20, 15); // 20 concurrent librarians, 15 operations each = 300 total requests

        // Requirements: 87% success rate, under 700ms
        assertPerformanceRequirements(result, 87.0, 700.0);
    }
}
