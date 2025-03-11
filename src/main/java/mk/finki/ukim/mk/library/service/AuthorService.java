package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.Author;
import mk.finki.ukim.mk.library.model.Dto.AuthorDto;

import java.util.List;
import java.util.Optional;

public interface AuthorService {

    List<Author> findAll();

    Optional<Author> findById(Long id);

    Optional<Author> save(AuthorDto authorDto);

    Optional<Author> update(Long id, AuthorDto authorDto);

    void deleteById(Long id);
}
