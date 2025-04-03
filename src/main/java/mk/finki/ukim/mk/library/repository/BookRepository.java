package mk.finki.ukim.mk.library.repository;


import mk.finki.ukim.mk.library.model.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository  extends JpaRepository<Book, Long> {
}
