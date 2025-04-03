package mk.finki.ukim.mk.library.service.domain.Impl;


import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.service.domain.AuthorService;
import mk.finki.ukim.mk.library.service.domain.BookService;
import mk.finki.ukim.mk.library.service.domain.CountryService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
    public Optional<Book> save(Book book) {
        return Optional.of(bookRepository.save(book));
    }

    @Override
    public Optional<Book> update(Book book) {
        return Optional.of(bookRepository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
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
}
