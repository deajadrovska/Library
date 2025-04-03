package mk.finki.ukim.mk.library.service.domain;

import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Category;

import java.util.List;
import java.util.Optional;

public interface BookService {

    List<Book> findAll();
    Optional<Book> findById(Long id);
    Optional<Book> save(Book book);
    Optional<Book> update(Book book);
    void deleteById(Long id);
    Optional<Book> markAsBorrowed(Long id);
    List<Category> findAllCategories();
}
