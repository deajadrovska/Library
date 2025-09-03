package mk.finki.ukim.mk.library.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import mk.finki.ukim.mk.library.config.IntegrationTestConfig;
import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Base class for performance testing providing common utilities for load testing,
 * concurrent request execution, and performance metrics collection.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
public abstract class BasePerformanceTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String userToken;
    protected String librarianToken;
    protected String uniqueUsername;
    protected String uniqueLibrarianUsername;

    /**
     * Performance test result containing metrics and statistics
     */
    public static class PerformanceResult {
        private final int totalRequests;
        private final long totalTimeMs;
        private final long minTimeMs;
        private final long maxTimeMs;
        private final double avgTimeMs;
        private final int successfulRequests;
        private final int failedRequests;
        private final double successRate;
        private final double requestsPerSecond;

        public PerformanceResult(int totalRequests, long totalTimeMs, long minTimeMs, 
                               long maxTimeMs, int successfulRequests, int failedRequests) {
            this.totalRequests = totalRequests;
            this.totalTimeMs = totalTimeMs;
            this.minTimeMs = minTimeMs;
            this.maxTimeMs = maxTimeMs;
            this.avgTimeMs = totalRequests > 0 ? (double) totalTimeMs / totalRequests : 0;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.successRate = totalRequests > 0 ? (double) successfulRequests / totalRequests * 100 : 0;
            this.requestsPerSecond = totalTimeMs > 0 ? (double) totalRequests / (totalTimeMs / 1000.0) : 0;
        }

        // Getters
        public int getTotalRequests() { return totalRequests; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public long getMinTimeMs() { return minTimeMs; }
        public long getMaxTimeMs() { return maxTimeMs; }
        public double getAvgTimeMs() { return avgTimeMs; }
        public int getSuccessfulRequests() { return successfulRequests; }
        public int getFailedRequests() { return failedRequests; }
        public double getSuccessRate() { return successRate; }
        public double getRequestsPerSecond() { return requestsPerSecond; }

        @Override
        public String toString() {
            return String.format(
                "Performance Results:\n" +
                "  Total Requests: %d\n" +
                "  Successful: %d (%.1f%%)\n" +
                "  Failed: %d\n" +
                "  Total Time: %d ms\n" +
                "  Average Response Time: %.2f ms\n" +
                "  Min Response Time: %d ms\n" +
                "  Max Response Time: %d ms\n" +
                "  Requests per Second: %.2f",
                totalRequests, successfulRequests, successRate, failedRequests,
                totalTimeMs, avgTimeMs, minTimeMs, maxTimeMs, requestsPerSecond
            );
        }
    }

    @BeforeEach
    void setUpAuthentication() throws Exception {
        // Create unique test users for each test to avoid conflicts
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueUserSuffix = timestamp.substring(timestamp.length() - 6); // Last 6 digits

        uniqueUsername = "perfuser" + uniqueUserSuffix;
        uniqueLibrarianUsername = "perflib" + uniqueUserSuffix;

        CreateUserDto userDto = new CreateUserDto(
                uniqueUsername, "password123", "password123",
                "Performance", "User", Role.ROLE_USER
        );
        CreateUserDto librarianDto = new CreateUserDto(
                uniqueLibrarianUsername, "password123", "password123",
                "Performance", "Librarian", Role.ROLE_LIBRARIAN
        );

        // Register users (expect 200 for successful registration)
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianDto)))
                .andExpect(status().isOk());

        // Login and get tokens using the unique usernames
        LoginUserDto userLogin = new LoginUserDto(uniqueUsername, "password123");
        LoginUserDto librarianLogin = new LoginUserDto(uniqueLibrarianUsername, "password123");

        MvcResult userResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult librarianResult = mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(librarianLogin)))
                .andExpect(status().isOk())
                .andReturn();

        userToken = objectMapper.readTree(userResult.getResponse().getContentAsString())
                .get("token").asText();
        librarianToken = objectMapper.readTree(librarianResult.getResponse().getContentAsString())
                .get("token").asText();
    }

    /**
     * Execute a load test with specified number of concurrent users and requests per user
     */
    protected PerformanceResult executeLoadTest(Supplier<Boolean> testOperation, 
                                              int concurrentUsers, 
                                              int requestsPerUser) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<CompletableFuture<List<Long>>> futures = new ArrayList<>();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);
        AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxTime = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        // Submit tasks for each concurrent user
        for (int i = 0; i < concurrentUsers; i++) {
            CompletableFuture<List<Long>> future = CompletableFuture.supplyAsync(() -> {
                List<Long> responseTimes = new ArrayList<>();
                
                for (int j = 0; j < requestsPerUser; j++) {
                    long requestStart = System.currentTimeMillis();
                    try {
                        boolean success = testOperation.get();
                        long responseTime = System.currentTimeMillis() - requestStart;
                        responseTimes.add(responseTime);
                        
                        if (success) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                        
                        // Update min/max times atomically
                        minTime.updateAndGet(current -> Math.min(current, responseTime));
                        maxTime.updateAndGet(current -> Math.max(current, responseTime));
                        totalTime.addAndGet(responseTime);
                        
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        long responseTime = System.currentTimeMillis() - requestStart;
                        responseTimes.add(responseTime);
                        totalTime.addAndGet(responseTime);
                    }
                }
                return responseTimes;
            }, executor);
            
            futures.add(future);
        }

        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long totalTestTime = endTime - startTime;

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        int totalRequests = concurrentUsers * requestsPerUser;
        return new PerformanceResult(
            totalRequests,
            totalTestTime,
            minTime.get() == Long.MAX_VALUE ? 0 : minTime.get(),
            maxTime.get(),
            successCount.get(),
            failureCount.get()
        );
    }

    /**
     * Assert that performance meets minimum requirements
     */
    protected void assertPerformanceRequirements(PerformanceResult result, 
                                                double minSuccessRate, 
                                                double maxAvgResponseTimeMs) {
        System.out.println("\n" + result.toString());
        
        if (result.getSuccessRate() < minSuccessRate) {
            throw new AssertionError(String.format(
                "Success rate %.1f%% is below minimum requirement of %.1f%%",
                result.getSuccessRate(), minSuccessRate
            ));
        }
        
        if (result.getAvgTimeMs() > maxAvgResponseTimeMs) {
            throw new AssertionError(String.format(
                "Average response time %.2f ms exceeds maximum requirement of %.2f ms",
                result.getAvgTimeMs(), maxAvgResponseTimeMs
            ));
        }
    }

    // Helper methods for flexible status matching
    protected ResultMatcher isOkOrNotFound() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status != 200 && status != 404) {
                throw new AssertionError("Expected status 200 or 404, but was " + status);
            }
        };
    }

    protected ResultMatcher isOkOrBadRequest() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status != 200 && status != 400) {
                throw new AssertionError("Expected status 200 or 400, but was " + status);
            }
        };
    }

    protected ResultMatcher isNoContentOrNotFound() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status != 204 && status != 404) {
                throw new AssertionError("Expected status 204 or 404, but was " + status);
            }
        };
    }
}
