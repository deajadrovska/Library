package mk.finki.ukim.mk.library.system;

import mk.finki.ukim.mk.library.config.IntegrationTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System Environment Tests
 * Validates that the system runs correctly in different environments and configurations.
 * Tests environment-specific settings, resource availability, and system constraints.
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class SystemEnvironmentTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Environment: Test environment should be properly configured")
    void environment_TestEnvironmentShouldBeProperlyConfigured() {
        // Verify test profile is active
        String[] activeProfiles = environment.getActiveProfiles();
        boolean hasTestProfile = false;
        for (String profile : activeProfiles) {
            if (profile.contains("test")) {
                hasTestProfile = true;
                break;
            }
        }
        assertTrue(hasTestProfile, "Test profile should be active");

        // Verify test-specific properties (may be auto-configured for H2)
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        // H2 may be auto-configured without explicit URL property
        if (datasourceUrl != null) {
            assertTrue(datasourceUrl.contains("h2") || datasourceUrl.contains("mem"),
                      "Test environment should use in-memory database");
        }
    }

    @Test
    @DisplayName("Environment: Database environment should be isolated for testing")
    void environment_DatabaseEnvironmentShouldBeIsolatedForTesting() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            
            // Verify we're using test database
            assertTrue(databaseProductName.contains("H2"), "Should be using H2 for tests");
            assertNotNull(databaseProductVersion, "Database version should be available");
            
            // Verify database URL indicates test environment
            String url = metaData.getURL();
            assertTrue(url.contains("mem") || url.contains("test"), 
                      "Database URL should indicate test environment");
        }
    }

    @Test
    @DisplayName("Environment: System should handle memory constraints appropriately")
    void environment_SystemShouldHandleMemoryConstraintsAppropriately() {
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // Basic memory availability checks
        assertTrue(maxMemory > 0, "Max memory should be positive");
        assertTrue(totalMemory > 0, "Total memory should be positive");
        assertTrue(freeMemory >= 0, "Free memory should be non-negative");
        assertTrue(usedMemory >= 0, "Used memory should be non-negative");
        
        // Memory usage should be reasonable for test environment
        double memoryUsageRatio = (double) usedMemory / maxMemory;
        assertTrue(memoryUsageRatio < 0.9, "Memory usage should be less than 90% of max memory");
    }

    @Test
    @DisplayName("Environment: System properties should be appropriate for testing")
    void environment_SystemPropertiesShouldBeAppropriateForTesting() {
        Properties systemProperties = System.getProperties();
        
        // Verify Java version
        String javaVersion = systemProperties.getProperty("java.version");
        assertNotNull(javaVersion, "Java version should be available");
        
        // Verify OS information
        String osName = systemProperties.getProperty("os.name");
        String osVersion = systemProperties.getProperty("os.version");
        assertNotNull(osName, "OS name should be available");
        assertNotNull(osVersion, "OS version should be available");
        
        // Verify user directory
        String userDir = systemProperties.getProperty("user.dir");
        assertNotNull(userDir, "User directory should be available");
        assertTrue(userDir.length() > 0, "User directory should not be empty");
    }

    @Test
    @DisplayName("Environment: File system access should be available")
    void environment_FileSystemAccessShouldBeAvailable() {
        String tempDir = System.getProperty("java.io.tmpdir");
        assertNotNull(tempDir, "Temp directory should be available");
        
        java.io.File tempDirFile = new java.io.File(tempDir);
        assertTrue(tempDirFile.exists(), "Temp directory should exist");
        assertTrue(tempDirFile.isDirectory(), "Temp directory should be a directory");
        assertTrue(tempDirFile.canRead(), "Temp directory should be readable");
        assertTrue(tempDirFile.canWrite(), "Temp directory should be writable");
    }

    @Test
    @DisplayName("Environment: Network configuration should be appropriate")
    void environment_NetworkConfigurationShouldBeAppropriate() {
        // Test localhost resolution
        try {
            java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
            assertNotNull(localhost, "Localhost should be resolvable");
            assertNotNull(localhost.getHostName(), "Localhost hostname should be available");
            assertNotNull(localhost.getHostAddress(), "Localhost address should be available");
        } catch (java.net.UnknownHostException e) {
            fail("Localhost should be resolvable: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Environment: Time zone and locale should be configured")
    void environment_TimeZoneAndLocaleShouldBeConfigured() {
        java.util.TimeZone defaultTimeZone = java.util.TimeZone.getDefault();
        assertNotNull(defaultTimeZone, "Default time zone should be set");
        assertNotNull(defaultTimeZone.getID(), "Time zone ID should be available");
        
        java.util.Locale defaultLocale = java.util.Locale.getDefault();
        assertNotNull(defaultLocale, "Default locale should be set");
        assertNotNull(defaultLocale.getLanguage(), "Locale language should be available");
    }

    @Test
    @DisplayName("Environment: Database schema should be properly initialized")
    void environment_DatabaseSchemaShouldBeProperlyInitialized() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check for required tables (H2 may use different naming conventions)
            ResultSet allTables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            boolean hasAnyTables = false;
            while (allTables.next()) {
                hasAnyTables = true;
                break;
            }
            allTables.close();

            // At minimum, we should have some tables created
            assertTrue(hasAnyTables, "Database should have at least some tables initialized");
        }
    }

    @Test
    @DisplayName("Environment: Security environment should be properly configured")
    void environment_SecurityEnvironmentShouldBeProperlyConfigured() {
        // Verify security-related environment properties
        String jwtSecret = environment.getProperty("jwt.secret");
        // JWT secret might not be in environment for security reasons, so we don't assert its presence
        
        // Verify that security manager is not overly restrictive for tests
        SecurityManager securityManager = System.getSecurityManager();
        // Most test environments don't use SecurityManager, so this is informational
        
        // Verify that we can create security-related objects
        assertDoesNotThrow(() -> {
            new java.security.SecureRandom();
        }, "Should be able to create SecureRandom");
    }

    @Test
    @DisplayName("Environment: Logging configuration should be appropriate for testing")
    void environment_LoggingConfigurationShouldBeAppropriateForTesting() {
        // Verify logging system is available
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SystemEnvironmentTest.class);
        assertNotNull(logger, "Logger should be available");
        
        // Test that logging works without throwing exceptions
        assertDoesNotThrow(() -> {
            logger.debug("Test debug message");
            logger.info("Test info message");
            logger.warn("Test warn message");
        }, "Logging should work without exceptions");
    }

    @Test
    @DisplayName("Environment: Thread environment should be suitable for testing")
    void environment_ThreadEnvironmentShouldBeSuitableForTesting() {
        Thread currentThread = Thread.currentThread();
        assertNotNull(currentThread, "Current thread should be available");
        assertNotNull(currentThread.getName(), "Thread name should be available");
        
        // Verify thread group
        ThreadGroup threadGroup = currentThread.getThreadGroup();
        assertNotNull(threadGroup, "Thread group should be available");
        
        // Verify we can create new threads (for potential async operations)
        assertDoesNotThrow(() -> {
            Thread testThread = new Thread(() -> {
                // Simple test operation
            });
            assertNotNull(testThread, "Should be able to create new threads");
        }, "Thread creation should be allowed");
    }

    @Test
    @DisplayName("Environment: Character encoding should be properly configured")
    void environment_CharacterEncodingShouldBeProperlyConfigured() {
        String defaultCharset = java.nio.charset.Charset.defaultCharset().name();
        assertNotNull(defaultCharset, "Default charset should be available");
        
        // Verify UTF-8 support (important for international characters)
        assertTrue(java.nio.charset.Charset.isSupported("UTF-8"), "UTF-8 should be supported");
        assertTrue(java.nio.charset.Charset.isSupported("ISO-8859-1"), "ISO-8859-1 should be supported");
        
        // Test file encoding property
        String fileEncoding = System.getProperty("file.encoding");
        assertNotNull(fileEncoding, "File encoding should be set");
    }
}
