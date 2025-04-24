package mk.finki.ukim.mk.library.repository;


import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.projections.AuthorNameProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<AuthorNameProjection> findAllProjectedBy();
}
