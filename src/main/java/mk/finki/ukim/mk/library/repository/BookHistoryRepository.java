package mk.finki.ukim.mk.library.repository;


import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.BookHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookHistoryRepository extends JpaRepository<BookHistory, Long> {
    List<BookHistory> findByBookOrderByModifiedAtDesc(Book book);

    @Modifying
    @Query("DELETE FROM BookHistory bh WHERE bh.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
