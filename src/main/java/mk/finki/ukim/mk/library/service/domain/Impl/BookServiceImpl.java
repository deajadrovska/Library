package mk.finki.ukim.mk.library.service.domain.Impl;

import mk.finki.ukim.mk.library.model.domain.*;
import mk.finki.ukim.mk.library.repository.BookHistoryRepository;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.repository.BooksByAuthorViewRepository;
import mk.finki.ukim.mk.library.service.domain.BookService;
import mk.finki.ukim.mk.library.service.domain.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookHistoryRepository bookHistoryRepository;
    private final UserService userService;
    private final BooksByAuthorViewRepository booksByAuthorViewRepository;

    public BookServiceImpl(BookRepository bookRepository,
                           BookHistoryRepository bookHistoryRepository,
                           UserService userService,
                           BooksByAuthorViewRepository booksByAuthorViewRepository) {
        this.bookRepository = bookRepository;
        this.bookHistoryRepository = bookHistoryRepository;
        this.userService = userService;
        this.booksByAuthorViewRepository = booksByAuthorViewRepository;
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<Book> save(Book book, String username) {
        User user = userService.findByUsername(username);
        Book savedBook = bookRepository.save(book);
        BookHistory history = new BookHistory(savedBook, user);
        bookHistoryRepository.save(history);
        this.refreshBooksByAuthorView();
        return Optional.of(savedBook);
    }

    @Override
    @Transactional
    public Optional<Book> update(Book book, String username) {
        User user = userService.findByUsername(username);
        Book savedBook = bookRepository.save(book);
        BookHistory history = new BookHistory(savedBook, user);
        bookHistoryRepository.save(history);
        this.refreshBooksByAuthorView();
        return Optional.of(savedBook);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
        this.refreshBooksByAuthorView();
    }

    @Override
    @Transactional
    public Optional<Book> markAsBorrowed(Long id) {
        return bookRepository.findById(id)
                .map(book -> {
                    if (book.getAvailableCopies() > 0) {
                        book.setAvailableCopies(book.getAvailableCopies() - 1);
                        return bookRepository.save(book);
                    }
                    return book;
                });
    }

    @Override
    public List<Category> findAllCategories() {
        return Arrays.asList(Category.values());
    }

    @Override
    public List<BookHistory> getBookHistory(Long bookId) {
        return bookRepository.findById(bookId)
                .map(bookHistoryRepository::findByBookOrderByModifiedAtDesc)
                .orElse(List.of());
    }

    @Override
    public void refreshBooksByAuthorView() {
        booksByAuthorViewRepository.refreshMaterializedView();
    }
}