package mk.finki.ukim.mk.library.model.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

@Data
@Entity
@Subselect("SELECT * FROM books_by_author")
@Immutable
public class BooksByAuthorView {

    @Id
    @Column(name = "author_id")
    private Long authorId;

    private String name;

    private String surname;

    @Column(name = "book_count")
    private Long bookCount;
}
