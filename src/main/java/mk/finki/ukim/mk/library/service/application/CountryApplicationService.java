package mk.finki.ukim.mk.library.service.application;

import mk.finki.ukim.mk.library.model.Dto.CreateCountryDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;

import java.util.List;
import java.util.Optional;

public interface CountryApplicationService {
    List<DisplayCountryDto> findAll();
    Optional<DisplayCountryDto> findById(Long id);
    Optional<DisplayCountryDto> save(CreateCountryDto countryDto);
    Optional<DisplayCountryDto> update(Long id, CreateCountryDto countryDto);
    void deleteById(Long id);
}
