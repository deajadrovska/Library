package mk.finki.ukim.mk.library.service.Impl;


import mk.finki.ukim.mk.library.model.Author;
import mk.finki.ukim.mk.library.model.Book;
import mk.finki.ukim.mk.library.model.Category;
import mk.finki.ukim.mk.library.model.Dto.BookDto;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.service.AuthorService;
import mk.finki.ukim.mk.library.service.BookService;
import mk.finki.ukim.mk.library.service.CountryService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

     private final BookRepository bookRepository;
     private final AuthorService authorService;
     private final CountryService countryService;

    public BookServiceImpl(BookRepository bookRepository, AuthorService authorService, CountryService countryService) {
        this.bookRepository = bookRepository;
        this.authorService = authorService;
        this.countryService = countryService;
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
    public Optional<Book> save(BookDto bookDto) {
        if (bookDto.getAuthorId() != null && authorService.findById(bookDto.getAuthorId()).isPresent()) {
            Author author = authorService.findById(bookDto.getAuthorId()).get();
            Book book = new Book(
                    bookDto.getName(),
                    bookDto.getCategory(),
                    author,
                    bookDto.getAvailableCopies()
            );
            return Optional.of(bookRepository.save(book));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Book> update(Long id, BookDto bookDto) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    if (bookDto.getName() != null) {
                        existingBook.setName(bookDto.getName());
                    }
                    if (bookDto.getCategory() != null) {
                        existingBook.setCategory(bookDto.getCategory());
                    }
                    if (bookDto.getAvailableCopies() != null) {
                        existingBook.setAvailableCopies(bookDto.getAvailableCopies());
                    }
                    if (bookDto.getAuthorId() != null) {
                        authorService.findById(bookDto.getAuthorId())
                                .ifPresent(existingBook::setAuthor);
                    }
                    return bookRepository.save(existingBook);
                });
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
