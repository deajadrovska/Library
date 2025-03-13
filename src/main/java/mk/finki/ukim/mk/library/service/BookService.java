package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.Book;
import mk.finki.ukim.mk.library.model.Category;
import mk.finki.ukim.mk.library.model.Dto.BookDto;

import java.util.List;
import java.util.Optional;

public interface BookService {

    List<Book> findAll();

    Optional<Book> findById(Long id);

    Optional<Book> save(BookDto bookDto);

    Optional<Book> update(Long id, BookDto bookDto);

    void deleteById(Long id);
    void softDeleteById(Long id);

    // Additional method to mark a book as borrowed (decrease available copies)
    Optional<Book> markAsBorrowed(Long id);

    // Additional method to get all categories
    List<Category> findAllCategories();
}
