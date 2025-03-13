package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    List<BookCopy> findByBookId(Long bookId);
    List<BookCopy> findByBookIdAndBorrowedFalse(Long bookId);
}
