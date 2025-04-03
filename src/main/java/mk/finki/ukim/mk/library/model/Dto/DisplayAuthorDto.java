package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.Author;

import java.util.List;
import java.util.stream.Collectors;

public record DisplayAuthorDto(
        Long id,
        String name,
        String surname,
        DisplayCountryDto country
) {
    public static DisplayAuthorDto from(Author author) {
        return new DisplayAuthorDto(
                author.getId(),
                author.getName(),
                author.getSurname(),
                DisplayCountryDto.from(author.getCountry())
        );
    }

    public static List<DisplayAuthorDto> from(List<Author> authors) {
        return authors.stream()
                .map(DisplayAuthorDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String surname() {
        return surname;
    }

    @Override
    public DisplayCountryDto country() {
        return country;
    }


}
