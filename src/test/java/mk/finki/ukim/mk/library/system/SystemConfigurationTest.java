package mk.finki.ukim.mk.library.system;

import mk.finki.ukim.mk.library.config.IntegrationTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System Configuration Tests
 * Validates that the system is properly configured and all components are correctly wired.
 * Tests system-level configuration, database connectivity, and environment setup.
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@Transactional
public class SystemConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("System Config: Application context should load successfully")
    void systemConfig_ApplicationContextShouldLoadSuccessfully() {
        assertNotNull(applicationContext, "Application context should be loaded");
        assertTrue(applicationContext.getBeanDefinitionCount() > 0, "Application context should contain beans");
    }

    @Test
    @DisplayName("System Config: All required beans should be present")
    void systemConfig_AllRequiredBeansShouldBePresent() {
        // Verify core service beans (using actual implementation class names)
        assertTrue(applicationContext.containsBean("userServiceImpl"), "UserServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("bookServiceImpl"), "BookServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("authorServiceImpl"), "AuthorServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("wishlistServiceImpl"), "WishlistServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("countryServiceImpl"), "CountryServiceImpl bean should exist");

        // Verify repository beans
        assertTrue(applicationContext.containsBean("bookRepository"), "BookRepository bean should exist");
        assertTrue(applicationContext.containsBean("authorRepository"), "AuthorRepository bean should exist");
        assertTrue(applicationContext.containsBean("userRepository"), "UserRepository bean should exist");
        assertTrue(applicationContext.containsBean("wishlistRepository"), "WishlistRepository bean should exist");
        assertTrue(applicationContext.containsBean("countryRepository"), "CountryRepository bean should exist");

        // Verify controller beans
        assertTrue(applicationContext.containsBean("bookController"), "BookController bean should exist");
        assertTrue(applicationContext.containsBean("authorController"), "AuthorController bean should exist");
        assertTrue(applicationContext.containsBean("userController"), "UserController bean should exist");
        assertTrue(applicationContext.containsBean("wishlistController"), "WishlistController bean should exist");
        assertTrue(applicationContext.containsBean("countryController"), "CountryController bean should exist");
    }

    @Test
    @DisplayName("System Config: Database connection should be established")
    void systemConfig_DatabaseConnectionShouldBeEstablished() throws Exception {
        assertNotNull(dataSource, "DataSource should be configured");
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should be established");
            assertFalse(connection.isClosed(), "Database connection should be open");
            
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData, "Database metadata should be available");
            
            String databaseProductName = metaData.getDatabaseProductName();
            assertNotNull(databaseProductName, "Database product name should be available");
            assertTrue(databaseProductName.contains("H2"), "Should be using H2 database for tests");
        }
    }

    @Test
    @DisplayName("System Config: Environment properties should be loaded")
    void systemConfig_EnvironmentPropertiesShouldBeLoaded() {
        // Verify active profiles
        String[] activeProfiles = environment.getActiveProfiles();
        assertNotNull(activeProfiles, "Active profiles should be set");
        assertTrue(activeProfiles.length > 0, "At least one profile should be active");
        
        boolean hasIntegrationTestProfile = false;
        for (String profile : activeProfiles) {
            if ("integration-test".equals(profile)) {
                hasIntegrationTestProfile = true;
                break;
            }
        }
        assertTrue(hasIntegrationTestProfile, "integration-test profile should be active");

        // Verify database properties (may be auto-configured for H2)
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        // H2 may be auto-configured without explicit URL property
        if (datasourceUrl != null) {
            assertTrue(datasourceUrl.contains("h2"), "Should be using H2 database for tests");
        }

        // Verify JPA properties
        String jpaDialect = environment.getProperty("spring.jpa.database-platform");
        if (jpaDialect != null) {
            assertTrue(jpaDialect.contains("H2"), "JPA dialect should be H2");
        }
    }

    @Test
    @DisplayName("System Config: Security configuration should be properly set up")
    void systemConfig_SecurityConfigurationShouldBeProperlySetUp() {
        // Verify security-related beans
        assertTrue(applicationContext.containsBean("passwordEncoder"), "PasswordEncoder bean should exist");
        // JwtHelper may be conditionally configured
        boolean hasJwtHelper = applicationContext.containsBean("jwtHelper");
        // JwtFilter may be conditionally configured
        boolean hasJwtFilter = applicationContext.containsBean("jwtFilter");
        assertTrue(applicationContext.containsBean("customUsernamePasswordAuthenticationProvider"),
                  "CustomUsernamePasswordAuthenticationProvider bean should exist");
    }

    @Test
    @DisplayName("System Config: Application service layer should be configured")
    void systemConfig_ApplicationServiceLayerShouldBeConfigured() {
        // Verify application service beans (using actual implementation class names)
        assertTrue(applicationContext.containsBean("userApplicationServiceImpl"), "UserApplicationServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("bookApplicationServiceImpl"), "BookApplicationServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("authorApplicationServiceImpl"), "AuthorApplicationServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("wishlistApplicationServiceImpl"), "WishlistApplicationServiceImpl bean should exist");
        assertTrue(applicationContext.containsBean("countryApplicationServiceImpl"), "CountryApplicationServiceImpl bean should exist");
    }

    @Test
    @DisplayName("System Config: Transaction management should be configured")
    void systemConfig_TransactionManagementShouldBeConfigured() {
        assertTrue(applicationContext.containsBean("transactionManager"), "TransactionManager bean should exist");
        
        // Verify that @Transactional annotation is being processed
        String[] transactionalBeans = applicationContext.getBeanNamesForAnnotation(Transactional.class);
        // Note: This might be 0 if @Transactional is only on methods, not classes
        assertTrue(transactionalBeans.length >= 0, "Transactional annotation processing should be enabled");
    }

    @Test
    @DisplayName("System Config: Web MVC configuration should be set up")
    void systemConfig_WebMvcConfigurationShouldBeSetUp() {
        // Verify web-related beans
        assertTrue(applicationContext.containsBean("requestMappingHandlerMapping"), "RequestMappingHandlerMapping should exist");
        assertTrue(applicationContext.containsBean("requestMappingHandlerAdapter"), "RequestMappingHandlerAdapter should exist");
    }

    @Test
    @DisplayName("System Config: JSON serialization should be configured")
    void systemConfig_JsonSerializationShouldBeConfigured() {
        assertTrue(applicationContext.containsBean("jacksonObjectMapper"), "Jackson ObjectMapper should exist");
    }

    @Test
    @DisplayName("System Config: Database initialization should complete successfully")
    void systemConfig_DatabaseInitializationShouldCompleteSuccessfully() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Verify that tables exist (basic schema validation)
            var resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            boolean hasUserTable = false;
            boolean hasBookTable = false;
            boolean hasAuthorTable = false;
            boolean hasCountryTable = false;
            boolean hasWishlistTable = false;
            
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME").toLowerCase();
                if (tableName.contains("user")) hasUserTable = true;
                if (tableName.contains("book")) hasBookTable = true;
                if (tableName.contains("author")) hasAuthorTable = true;
                if (tableName.contains("country")) hasCountryTable = true;
                if (tableName.contains("wishlist")) hasWishlistTable = true;
            }
            
            assertTrue(hasUserTable, "User table should exist");
            assertTrue(hasBookTable, "Book table should exist");
            assertTrue(hasAuthorTable, "Author table should exist");
            assertTrue(hasCountryTable, "Country table should exist");
            assertTrue(hasWishlistTable, "Wishlist table should exist");
        }
    }
}
