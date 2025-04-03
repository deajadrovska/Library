package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Country;

public record CreateAuthorDto(
        String name,
        String surname,
        Long countryId
) {
    public Author toAuthor(Country country) {
        Author author = new Author();
        author.setName(name);
        author.setSurname(surname);
        author.setCountry(country);
        return author;
    }
}
