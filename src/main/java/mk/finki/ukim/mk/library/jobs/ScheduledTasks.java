package mk.finki.ukim.mk.library.jobs;

import mk.finki.ukim.mk.library.repository.BooksByAuthorViewRepository;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final BooksByAuthorViewRepository booksByAuthorViewRepository;
    private final Environment environment;

    public ScheduledTasks(BooksByAuthorViewRepository booksByAuthorViewRepository, Environment environment) {
        this.booksByAuthorViewRepository = booksByAuthorViewRepository;
        this.environment = environment;
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void refreshBooksbyAuthorView() {
        // Only refresh materialized view in PostgreSQL production environment
        boolean isTestProfile = environment.acceptsProfiles("test");

        if (!isTestProfile) {
            try {
                booksByAuthorViewRepository.refreshMaterializedViewPostgreSQL();
            } catch (Exception e) {
                System.err.println("Warning: Could not refresh materialized view: " + e.getMessage());
            }
        }
    }
}
