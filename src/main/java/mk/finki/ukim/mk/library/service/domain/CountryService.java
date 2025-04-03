package mk.finki.ukim.mk.library.service.domain;



import mk.finki.ukim.mk.library.model.domain.Country;

import java.util.List;
import java.util.Optional;

public interface CountryService {

    List<Country> findAll();
    Optional<Country> findById(Long id);
    Optional<Country> save(Country country);
    Optional<Country> update(Country country);
    void deleteById(Long id);
}
