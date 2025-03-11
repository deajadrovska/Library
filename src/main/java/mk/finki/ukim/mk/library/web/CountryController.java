package mk.finki.ukim.mk.library.web;

import mk.finki.ukim.mk.library.model.Country;
import mk.finki.ukim.mk.library.service.CountryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public List<Country> findAll() {
        return countryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Country> findById(@PathVariable Long id) {
        return countryService.findById(id)
                .map(country -> ResponseEntity.ok().body(country))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<Country> save(@RequestBody Country country) {
        return countryService.save(country)
                .map(savedCountry -> ResponseEntity.ok().body(savedCountry))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Country> update(@PathVariable Long id, @RequestBody Country country) {
        return countryService.update(id, country)
                .map(updatedCountry -> ResponseEntity.ok().body(updatedCountry))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (countryService.findById(id).isPresent()) {
            countryService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
