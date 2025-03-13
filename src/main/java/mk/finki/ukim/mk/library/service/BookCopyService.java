package mk.finki.ukim.mk.library.service;

import mk.finki.ukim.mk.library.model.BookCopy;
import mk.finki.ukim.mk.library.model.Dto.BookCopyDto;

import java.util.List;
import java.util.Optional;

public interface BookCopyService {
    List<BookCopy> findAll();

    Optional<BookCopy> findById(Long id);

    Optional<BookCopy> save(BookCopyDto bookCopyDto);

    Optional<BookCopy> markAsBorrowed(Long id);

    Optional<BookCopy> markAsReturned(Long id);

    void deleteById(Long id);

    List<BookCopy> findByBookId(Long bookId);

    List<BookCopy> findAvailableCopiesByBookId(Long bookId);
}
