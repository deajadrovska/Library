package mk.finki.ukim.mk.library.service.domain.Impl;


import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.repository.CountryRepository;
import mk.finki.ukim.mk.library.service.domain.CountryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    @Override
    public Optional<Country> findById(Long id) {
        return countryRepository.findById(id);
    }

    @Override
    public Optional<Country> save(Country country) {
        return Optional.of(countryRepository.save(country));
    }

    @Override
    public Optional<Country> update(Country country) {
        return Optional.of(countryRepository.save(country));
    }

    @Override
    public void deleteById(Long id) {
        countryRepository.deleteById(id);
    }
}
