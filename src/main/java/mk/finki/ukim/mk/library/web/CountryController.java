package mk.finki.ukim.mk.library.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mk.finki.ukim.mk.library.model.Dto.CreateCountryDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayCountryDto;
import mk.finki.ukim.mk.library.service.application.CountryApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@Tag(name = "Country Management", description = "APIs for managing countries")
public class CountryController {

    private final CountryApplicationService countryService;

    public CountryController(CountryApplicationService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    @Operation(summary = "Get all countries", description = "Retrieve a list of all countries")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved countries")
    public List<DisplayCountryDto> findAll() {
        return countryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get country by ID", description = "Retrieve a specific country by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved country")
    @ApiResponse(responseCode = "404", description = "Country not found")
    public ResponseEntity<DisplayCountryDto> findById(@PathVariable Long id) {
        return countryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @Operation(summary = "Create a new country", description = "Add a new country to the system")
    @ApiResponse(responseCode = "200", description = "Country successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid country data")
    public ResponseEntity<DisplayCountryDto> save(@RequestBody CreateCountryDto countryDto) {
        return countryService.save(countryDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/edit/{id}")
    @Operation(summary = "Update an existing country", description = "Update details of an existing country")
    @ApiResponse(responseCode = "200", description = "Country successfully updated")
    @ApiResponse(responseCode = "404", description = "Country not found")
    public ResponseEntity<DisplayCountryDto> update(@PathVariable Long id, @RequestBody CreateCountryDto countryDto) {
        return countryService.update(id, countryDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete a country", description = "Remove a country from the system")
    @ApiResponse(responseCode = "204", description = "Country successfully deleted")
    @ApiResponse(responseCode = "404", description = "Country not found")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (countryService.findById(id).isPresent()) {
            countryService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}