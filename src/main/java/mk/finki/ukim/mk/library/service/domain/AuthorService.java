package mk.finki.ukim.mk.library.service.domain;

import mk.finki.ukim.mk.library.model.domain.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorService {

    List<Author> findAll();
    Optional<Author> findById(Long id);
    Optional<Author> save(Author author);
    Optional<Author> update(Author author);
    void deleteById(Long id);
}
