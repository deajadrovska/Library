package mk.finki.ukim.mk.library.repository;


import mk.finki.ukim.mk.library.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}
