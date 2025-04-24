package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.model.views.AuthorsByCountryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorsByCountryViewRepository extends JpaRepository<AuthorsByCountryView, Long> {
    // No need for a refresh method here since it's handled by the database triggers
}
