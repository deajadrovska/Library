package mk.finki.ukim.mk.library.service.domain;

import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.BookHistory;
import mk.finki.ukim.mk.library.model.domain.Category;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookService {

    List<Book> findAll();
    Optional<Book> findById(Long id);

    //dodaden username za changot da se zapazi
    Optional<Book> save(Book book, String username);
    Optional<Book> update(Book book, String username);


    void deleteById(Long id);
    Optional<Book> markAsBorrowed(Long id);
    List<Category> findAllCategories();


    //for the bookgistory addition
    List<BookHistory> getBookHistory(Long bookId);
}
