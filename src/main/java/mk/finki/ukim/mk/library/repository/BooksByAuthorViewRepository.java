package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.model.views.BooksByAuthorView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BooksByAuthorViewRepository extends JpaRepository<BooksByAuthorView, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "REFRESH MATERIALIZED VIEW books_by_author", nativeQuery = true)
    void refreshMaterializedViewPostgreSQL();

    // For H2 test environment - no-op since regular views auto-update
    default void refreshMaterializedView() {
        // This method will be overridden by service implementation
        // to handle database-specific refresh logic
    }
}
