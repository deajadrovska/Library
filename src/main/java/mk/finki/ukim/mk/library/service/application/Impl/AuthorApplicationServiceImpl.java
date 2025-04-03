package mk.finki.ukim.mk.library.service.application.Impl;

import mk.finki.ukim.mk.library.model.Dto.CreateAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthorDto;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.service.application.AuthorApplicationService;
import mk.finki.ukim.mk.library.service.domain.AuthorService;
import mk.finki.ukim.mk.library.service.domain.CountryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorApplicationServiceImpl implements AuthorApplicationService {

    private final AuthorService authorService;
    private final CountryService countryService;

    public AuthorApplicationServiceImpl(AuthorService authorService, CountryService countryService) {
        this.authorService = authorService;
        this.countryService = countryService;
    }

    @Override
    public List<DisplayAuthorDto> findAll() {
        return authorService.findAll().stream()
                .map(DisplayAuthorDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DisplayAuthorDto> findById(Long id) {
        return authorService.findById(id)
                .map(DisplayAuthorDto::from);
    }

    @Override
    public Optional<DisplayAuthorDto> save(CreateAuthorDto authorDto) {
        return countryService.findById(authorDto.countryId())
                .map(country -> {
                    Author author = authorDto.toAuthor(country);
                    return authorService.save(author);
                })
                .orElse(Optional.empty())
                .map(DisplayAuthorDto::from);
    }

    @Override
    public Optional<DisplayAuthorDto> update(Long id, CreateAuthorDto authorDto) {
        return authorService.findById(id)
                .flatMap(existingAuthor -> countryService.findById(authorDto.countryId())
                        .map(country -> {
                            Author author = authorDto.toAuthor(country);
                            author.setId(id);
                            return authorService.update(author);
                        })
                        .orElse(Optional.empty())
                )
                .map(DisplayAuthorDto::from);
    }

    @Override
    public void deleteById(Long id) {
        authorService.deleteById(id);
    }
}
