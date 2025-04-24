package mk.finki.ukim.mk.library.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookHistoryDto;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.model.views.BooksByAuthorView;
import mk.finki.ukim.mk.library.repository.BooksByAuthorViewRepository;
import mk.finki.ukim.mk.library.service.application.BookApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book", description = "Book management endpoints")
public class BookController {

    private final BookApplicationService bookService;
    private final BooksByAuthorViewRepository booksByAuthorViewRepository;

    public BookController(BookApplicationService bookService, BooksByAuthorViewRepository booksByAuthorViewRepository) {
        this.bookService = bookService;
        this.booksByAuthorViewRepository = booksByAuthorViewRepository;
    }

    @GetMapping
    @Operation(summary = "Find all books", description = "Returns all books in the library")
    public List<DisplayBookDto> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find book by ID", description = "Returns a book by its ID")
    public ResponseEntity<DisplayBookDto> findById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @Operation(summary = "Add a new book", description = "Adds a new book to the library")
    public ResponseEntity<DisplayBookDto> save(@RequestBody CreateBookDto bookDto) {
        // Remove Principal parameter
        return bookService.save(bookDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @Operation(summary = "Edit a book", description = "Updates an existing book's information")
    public ResponseEntity<DisplayBookDto> update(@PathVariable Long id, @RequestBody CreateBookDto bookDto) {
        // Remove Principal parameter
        return bookService.update(id, bookDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @Operation(summary = "Delete a book", description = "Removes a book from the library")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (bookService.findById(id).isPresent()) {
            bookService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/mark-as-borrowed")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @Operation(summary = "Mark book as borrowed", description = "Decreases the available copies count by 1")
    public ResponseEntity<DisplayBookDto> markAsBorrowed(@PathVariable Long id) {
        return bookService.markAsBorrowed(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all book categories", description = "Returns all available book categories")
    public List<Category> findAllCategories() {
        return bookService.findAllCategories();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get book change history", description = "Returns the complete change history for a book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<List<DisplayBookHistoryDto>> getBookHistory(@PathVariable Long id) {
        if (bookService.findById(id).isPresent()) {
            return ResponseEntity.ok(bookService.getBookHistory(id));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/by-author")
    @Operation(summary = "Get books count by author",
            description = "Returns the number of books for each author from a materialized view that refreshes hourly")
    public ResponseEntity<List<BooksByAuthorView>> getBooksCountByAuthor() {
        return ResponseEntity.ok(booksByAuthorViewRepository.findAll());
    }
}