package mk.finki.ukim.mk.library.jobs;

import mk.finki.ukim.mk.library.repository.BooksByAuthorViewRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final BooksByAuthorViewRepository booksByAuthorViewRepository;

    public ScheduledTasks(BooksByAuthorViewRepository booksByAuthorViewRepository) {
        this.booksByAuthorViewRepository = booksByAuthorViewRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void refreshBooksbyAuthorView() {
        booksByAuthorViewRepository.refreshMaterializedView();
    }
}
