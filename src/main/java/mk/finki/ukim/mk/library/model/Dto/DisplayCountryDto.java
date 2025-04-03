package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.Country;

import java.util.List;
import java.util.stream.Collectors;

public record DisplayCountryDto(
        Long id,
        String name,
        String continent
) {
    public static DisplayCountryDto from(Country country) {
        return new DisplayCountryDto(
                country.getId(),
                country.getName(),
                country.getContinent()
        );
    }

    public static List<DisplayCountryDto> from(List<Country> countries) {
        return countries.stream()
                .map(DisplayCountryDto::from)
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
    public String continent() {
        return continent;
    }
}
