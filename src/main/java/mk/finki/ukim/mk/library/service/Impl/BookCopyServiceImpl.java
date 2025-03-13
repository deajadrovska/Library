package mk.finki.ukim.mk.library.service.Impl;

import mk.finki.ukim.mk.library.model.BookCopy;
import mk.finki.ukim.mk.library.model.Dto.BookCopyDto;
import mk.finki.ukim.mk.library.repository.BookCopyRepository;
import mk.finki.ukim.mk.library.service.BookCopyService;
import mk.finki.ukim.mk.library.service.BookService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookCopyServiceImpl implements BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookService bookService;

    public BookCopyServiceImpl(BookCopyRepository bookCopyRepository, @Lazy BookService bookService) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookService = bookService;
    }

    @Override
    public List<BookCopy> findAll() {
        return bookCopyRepository.findAll();
    }

    @Override
    public Optional<BookCopy> findById(Long id) {
        return bookCopyRepository.findById(id);
    }

    @Override
    public Optional<BookCopy> save(BookCopyDto bookCopyDto) {
        if (bookCopyDto.getBookId() != null) {
            return bookService.findById(bookCopyDto.getBookId())
                    .map(book -> {
                        BookCopy bookCopy = new BookCopy(book, bookCopyDto.getCondition());
                        return bookCopyRepository.save(bookCopy);
                    });
        }
        return Optional.empty();
    }

    @Override
    public Optional<BookCopy> markAsBorrowed(Long id) {
        return bookCopyRepository.findById(id)
                .map(bookCopy -> {
                    if (!bookCopy.getBorrowed()) {
                        bookCopy.setBorrowed(true);
                        return bookCopyRepository.save(bookCopy);
                    }
                    return bookCopy;
                });
    }

    @Override
    public Optional<BookCopy> markAsReturned(Long id) {
        return bookCopyRepository.findById(id)
                .map(bookCopy -> {
                    if (bookCopy.getBorrowed()) {
                        bookCopy.setBorrowed(false);
                        return bookCopyRepository.save(bookCopy);
                    }
                    return bookCopy;
                });
    }

    @Override
    public void deleteById(Long id) {
        bookCopyRepository.deleteById(id);
    }

    @Override
    public List<BookCopy> findByBookId(Long bookId) {
        return bookCopyRepository.findByBookId(bookId);
    }

    @Override
    public List<BookCopy> findAvailableCopiesByBookId(Long bookId) {
        return bookCopyRepository.findByBookIdAndBorrowedFalse(bookId);
    }
}
