package mk.finki.ukim.mk.library.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mk.finki.ukim.mk.library.model.Dto.CreateAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthorDto;
import mk.finki.ukim.mk.library.service.application.AuthorApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Author Management", description = "APIs for managing authors")
public class AuthorController {

    private final AuthorApplicationService authorService;

    public AuthorController(AuthorApplicationService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    @Operation(summary = "Get all authors", description = "Retrieve a list of all authors")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved authors")
    public List<DisplayAuthorDto> findAll() {
        return authorService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID", description = "Retrieve a specific author by their ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved author")
    @ApiResponse(responseCode = "404", description = "Author not found")
    public ResponseEntity<DisplayAuthorDto> findById(@PathVariable Long id) {
        return authorService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @Operation(summary = "Create a new author", description = "Add a new author to the system")
    @ApiResponse(responseCode = "200", description = "Author successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid author data")
    public ResponseEntity<DisplayAuthorDto> save(@RequestBody CreateAuthorDto authorDto) {
        return authorService.save(authorDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/edit/{id}")
    @Operation(summary = "Update an existing author", description = "Update details of an existing author")
    @ApiResponse(responseCode = "200", description = "Author successfully updated")
    @ApiResponse(responseCode = "404", description = "Author not found")
    public ResponseEntity<DisplayAuthorDto> update(@PathVariable Long id, @RequestBody CreateAuthorDto authorDto) {
        return authorService.update(id, authorDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete an author", description = "Remove an author from the system")
    @ApiResponse(responseCode = "204", description = "Author successfully deleted")
    @ApiResponse(responseCode = "404", description = "Author not found")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (authorService.findById(id).isPresent()) {
            authorService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}