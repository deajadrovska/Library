package mk.finki.ukim.mk.library.service.application.Impl;

import mk.finki.ukim.mk.library.model.Dto.CreateCountryDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.service.application.CountryApplicationService;
import mk.finki.ukim.mk.library.service.domain.CountryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryApplicationServiceImpl implements CountryApplicationService {

    private final CountryService countryService;

    public CountryApplicationServiceImpl(CountryService countryService) {
        this.countryService = countryService;
    }

    @Override
    public List<DisplayCountryDto> findAll() {
        return countryService.findAll().stream()
                .map(DisplayCountryDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DisplayCountryDto> findById(Long id) {
        return countryService.findById(id)
                .map(DisplayCountryDto::from);
    }

    @Override
    public Optional<DisplayCountryDto> save(CreateCountryDto countryDto) {
        Country country = countryDto.toCountry();
        return countryService.save(country)
                .map(DisplayCountryDto::from);
    }

    @Override
    public Optional<DisplayCountryDto> update(Long id, CreateCountryDto countryDto) {
        return countryService.findById(id)
                .map(existingCountry -> {
                    Country country = countryDto.toCountry();
                    country.setId(id);
                    return countryService.update(country);
                })
                .orElse(Optional.empty())
                .map(DisplayCountryDto::from);
    }

    @Override
    public void deleteById(Long id) {
        countryService.deleteById(id);
    }
}