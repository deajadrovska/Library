package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.Country;

public record CreateCountryDto(
        String name,
        String continent
) {
    public Country toCountry() {
        Country country = new Country();
        country.setName(name);
        country.setContinent(continent);
        return country;
    }
}
