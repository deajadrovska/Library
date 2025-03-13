package mk.finki.ukim.mk.library.web;


import mk.finki.ukim.mk.library.model.BookCopy;
import mk.finki.ukim.mk.library.model.Dto.BookCopyDto;
import mk.finki.ukim.mk.library.service.BookCopyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-copies")
public class BookCopyController {


   private final BookCopyService bookCopyService;

    public BookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    @GetMapping
    public List<BookCopy> findAll() {
        return bookCopyService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookCopy> findById(@PathVariable Long id) {
        return bookCopyService.findById(id)
                .map(bookCopy -> ResponseEntity.ok().body(bookCopy))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/book/{bookId}")
    public List<BookCopy> findByBookId(@PathVariable Long bookId) {
        return bookCopyService.findByBookId(bookId);
    }

    @GetMapping("/book/{bookId}/available")
    public List<BookCopy> findAvailableCopiesByBookId(@PathVariable Long bookId) {
        return bookCopyService.findAvailableCopiesByBookId(bookId);
    }

    @PostMapping("/add")
    public ResponseEntity<BookCopy> save(@RequestBody BookCopyDto bookCopyDto) {
        return bookCopyService.save(bookCopyDto)
                .map(savedBookCopy -> ResponseEntity.ok().body(savedBookCopy))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/borrow/{id}")
    public ResponseEntity<BookCopy> markAsBorrowed(@PathVariable Long id) {
        return bookCopyService.markAsBorrowed(id)
                .map(borrowedBookCopy -> ResponseEntity.ok().body(borrowedBookCopy))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/return/{id}")
    public ResponseEntity<BookCopy> markAsReturned(@PathVariable Long id) {
        return bookCopyService.markAsReturned(id)
                .map(returnedBookCopy -> ResponseEntity.ok().body(returnedBookCopy))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (bookCopyService.findById(id).isPresent()) {
            bookCopyService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
