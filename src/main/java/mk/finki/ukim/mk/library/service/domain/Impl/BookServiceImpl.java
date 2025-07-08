package mk.finki.ukim.mk.library.service.domain.Impl;

import mk.finki.ukim.mk.library.config.UserContext;
import mk.finki.ukim.mk.library.model.domain.*;
import mk.finki.ukim.mk.library.repository.BookHistoryRepository;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.repository.BooksByAuthorViewRepository;
import mk.finki.ukim.mk.library.service.domain.BookService;
import mk.finki.ukim.mk.library.service.domain.UserService;
import org.springframework.core.env.Environment;
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
    private final UserContext userContext;
    private final Environment environment;

    public BookServiceImpl(BookRepository bookRepository,
                           BookHistoryRepository bookHistoryRepository,
                           UserService userService,
                           BooksByAuthorViewRepository booksByAuthorViewRepository,
                           UserContext userContext,
                           Environment environment) {
        this.bookRepository = bookRepository;
        this.bookHistoryRepository = bookHistoryRepository;
        this.userService = userService;
        this.booksByAuthorViewRepository = booksByAuthorViewRepository;
        this.userContext = userContext;
        this.environment = environment;
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
//        User user = userContext.getCurrentUser();
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
//        User user = userContext.getCurrentUser();
        Book savedBook = bookRepository.save(book);
        BookHistory history = new BookHistory(savedBook, user);
        bookHistoryRepository.save(history);
        this.refreshBooksByAuthorView();
        return Optional.of(savedBook);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        bookHistoryRepository.deleteByBookId(id);
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
        // Only refresh materialized view in PostgreSQL production environment
        // H2 test environment uses regular views that auto-update
        boolean isTestProfile = environment.acceptsProfiles("test");

        if (!isTestProfile) {
            // Production PostgreSQL environment - refresh materialized view
            try {
                booksByAuthorViewRepository.refreshMaterializedViewPostgreSQL();
            } catch (Exception e) {
                // Log error but don't fail the operation
                System.err.println("Warning: Could not refresh materialized view: " + e.getMessage());
            }
        }
        // For test environment (H2), do nothing as regular views auto-update
    }
}