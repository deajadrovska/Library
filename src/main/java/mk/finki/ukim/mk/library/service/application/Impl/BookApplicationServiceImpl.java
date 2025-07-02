package mk.finki.ukim.mk.library.service.application.Impl;

import mk.finki.ukim.mk.library.config.UserContext;
import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookHistoryDto;
import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Category;
import mk.finki.ukim.mk.library.service.application.BookApplicationService;
import mk.finki.ukim.mk.library.service.domain.AuthorService;
import mk.finki.ukim.mk.library.service.domain.BookService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookApplicationServiceImpl implements BookApplicationService {

    private final BookService bookService;
    private final AuthorService authorService;
    private final UserContext userContext;

    public BookApplicationServiceImpl(BookService bookService, AuthorService authorService, UserContext userContext) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.userContext = userContext;
    }

    @Override
    public List<DisplayBookDto> findAll() {
        return bookService.findAll().stream()
                .map(DisplayBookDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DisplayBookDto> findById(Long id) {
        return bookService.findById(id)
                .map(DisplayBookDto::from);
    }

    @Override
    public Optional<DisplayBookDto> save(CreateBookDto bookDto) {
        String username = userContext.getCurrentUsername();

        return authorService.findById(bookDto.authorId())
                .map(author -> {
                    Book book = bookDto.toBook(author);
//                    return bookService.save(book, username);
                    return bookService.save(book, "dj");
                })
                .orElse(Optional.empty())
                .map(DisplayBookDto::from);
    }

    @Override
    public Optional<DisplayBookDto> update(Long id, CreateBookDto bookDto) {
        String username = userContext.getCurrentUsername();

        return bookService.findById(id)
                .flatMap(existingBook -> authorService.findById(bookDto.authorId())
                        .map(author -> {
                            Book book = bookDto.toBook(author);
                            book.setId(id);
//                            return bookService.update(book, username);
                            return bookService.save(book, "dj");
                        })
                        .orElse(Optional.empty())
                )
                .map(DisplayBookDto::from);
    }

    @Override
    public void deleteById(Long id) {
        bookService.deleteById(id);
    }

    @Override
    public Optional<DisplayBookDto> markAsBorrowed(Long id) {
        return bookService.markAsBorrowed(id)
                .map(DisplayBookDto::from);
    }

    @Override
    public List<Category> findAllCategories() {
        return bookService.findAllCategories();
    }

    @Override
    public List<DisplayBookHistoryDto> getBookHistory(Long bookId) {
        return DisplayBookHistoryDto.from(bookService.getBookHistory(bookId));
    }
}
