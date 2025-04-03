package mk.finki.ukim.mk.library.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.service.application.BookApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Management", description = "APIs for managing books")
public class BookController {

    private final BookApplicationService bookService;

    public BookController(BookApplicationService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve a list of all books")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved books")
    public List<DisplayBookDto> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved book")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<DisplayBookDto> findById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @Operation(summary = "Create a new book", description = "Add a new book to the system")
    @ApiResponse(responseCode = "200", description = "Book successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid book data")
    public ResponseEntity<DisplayBookDto> save(@RequestBody CreateBookDto bookDto) {
        return bookService.save(bookDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/edit/{id}")
    @Operation(summary = "Update an existing book", description = "Update details of an existing book")
    @ApiResponse(responseCode = "200", description = "Book successfully updated")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<DisplayBookDto> update(@PathVariable Long id, @RequestBody CreateBookDto bookDto) {
        return bookService.update(id, bookDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete a book", description = "Remove a book from the system")
    @ApiResponse(responseCode = "204", description = "Book successfully deleted")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (bookService.findById(id).isPresent()) {
            bookService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/mark-as-borrowed/{id}")
    @Operation(summary = "Mark book as borrowed", description = "Update the status of a book to borrowed")
    @ApiResponse(responseCode = "200", description = "Book successfully marked as borrowed")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<DisplayBookDto> markAsBorrowed(@PathVariable Long id) {
        return bookService.markAsBorrowed(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all book categories", description = "Retrieve a list of all book categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    public List<Category> findAllCategories() {
        return bookService.findAllCategories();
    }
}