package mk.finki.ukim.mk.library.service.Impl;


import mk.finki.ukim.mk.library.model.Author;
import mk.finki.ukim.mk.library.model.Dto.AuthorDto;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.service.AuthorService;
import mk.finki.ukim.mk.library.service.CountryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final CountryService countryService;

    public AuthorServiceImpl(AuthorRepository authorRepository, CountryService countryService) {
        this.authorRepository = authorRepository;
        this.countryService = countryService;
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
    public Optional<Author> save(AuthorDto authorDto) {
        if (authorDto.getCountryId() != null && countryService.findById(authorDto.getCountryId()).isPresent()) {
            Author author = new Author(
                    authorDto.getName(),
                    authorDto.getSurname(),
                    countryService.findById(authorDto.getCountryId()).get()
            );
            return Optional.of(authorRepository.save(author));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Author> update(Long id, AuthorDto authorDto) {
        return authorRepository.findById(id)
                .map(existingAuthor -> {
                    if (authorDto.getName() != null) {
                        existingAuthor.setName(authorDto.getName());
                    }
                    if (authorDto.getSurname() != null) {
                        existingAuthor.setSurname(authorDto.getSurname());
                    }
                    if (authorDto.getCountryId() != null) {
                        countryService.findById(authorDto.getCountryId())
                                .ifPresent(existingAuthor::setCountry);
                    }
                    return authorRepository.save(existingAuthor);
                });
    }

    @Override
    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }
}
