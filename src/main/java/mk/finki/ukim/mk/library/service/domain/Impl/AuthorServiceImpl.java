package mk.finki.ukim.mk.library.service.domain.Impl;


import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.projections.AuthorNameProjection;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.service.domain.AuthorService;
import mk.finki.ukim.mk.library.service.domain.CountryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    @Override
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    @Override
    public Optional<Author> save(Author author) {
        return Optional.of(authorRepository.save(author));
    }

    @Override
    public Optional<Author> update(Author author) {
        return Optional.of(authorRepository.save(author));
    }

    @Override
    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }

    @Override
    public List<AuthorNameProjection> getAllAuthorNames() {
        return authorRepository.findAllProjectedBy();
    }
}
