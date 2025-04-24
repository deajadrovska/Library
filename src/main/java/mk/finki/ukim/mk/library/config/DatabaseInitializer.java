package mk.finki.ukim.mk.library.config;


import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // Create books_by_author view
            jdbcTemplate.execute(
                    "CREATE MATERIALIZED VIEW IF NOT EXISTS books_by_author AS " +
                            "SELECT a.id as author_id, a.name, a.surname, COUNT(b.id) as book_count " +
                            "FROM author a " +
                            "LEFT JOIN book b ON a.id = b.author_id " +
                            "GROUP BY a.id, a.name, a.surname " +
                            "WITH DATA"
            );

            // Create index for better performance
            jdbcTemplate.execute(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_books_by_author ON books_by_author (author_id)"
            );

            // Create authors_by_country view
            jdbcTemplate.execute(
                    "CREATE MATERIALIZED VIEW IF NOT EXISTS authors_by_country AS " +
                            "SELECT c.id as country_id, c.name as country_name, COUNT(a.id) as author_count " +
                            "FROM country c " +
                            "LEFT JOIN author a ON c.id = a.country_id " +
                            "GROUP BY c.id, c.name " +
                            "WITH DATA"
            );

            // Create index for better performance
            jdbcTemplate.execute(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_authors_by_country ON authors_by_country (country_id)"
            );

            // Function to refresh the materialized view
            jdbcTemplate.execute(
                    "CREATE OR REPLACE FUNCTION refresh_authors_by_country() " +
                            "RETURNS TRIGGER AS $$ " +
                            "BEGIN " +
                            "    REFRESH MATERIALIZED VIEW authors_by_country; " +
                            "    RETURN NULL; " +
                            "END; " +
                            "$$ LANGUAGE plpgsql"
            );

            // Triggers to refresh the view
            jdbcTemplate.execute(
                    "DROP TRIGGER IF EXISTS refresh_authors_by_country_insert ON author"
            );
            jdbcTemplate.execute(
                    "CREATE TRIGGER refresh_authors_by_country_insert " +
                            "AFTER INSERT ON author " +
                            "FOR EACH STATEMENT " +
                            "EXECUTE FUNCTION refresh_authors_by_country()"
            );

            jdbcTemplate.execute(
                    "DROP TRIGGER IF EXISTS refresh_authors_by_country_update ON author"
            );
            jdbcTemplate.execute(
                    "CREATE TRIGGER refresh_authors_by_country_update " +
                            "AFTER UPDATE ON author " +
                            "FOR EACH STATEMENT " +
                            "EXECUTE FUNCTION refresh_authors_by_country()"
            );

            jdbcTemplate.execute(
                    "DROP TRIGGER IF EXISTS refresh_authors_by_country_delete ON author"
            );
            jdbcTemplate.execute(
                    "CREATE TRIGGER refresh_authors_by_country_delete " +
                            "AFTER DELETE ON author " +
                            "FOR EACH STATEMENT " +
                            "EXECUTE FUNCTION refresh_authors_by_country()"
            );

            System.out.println("Database views and triggers successfully initialized");
        } catch (Exception e) {
            System.err.println("Error initializing database views: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
