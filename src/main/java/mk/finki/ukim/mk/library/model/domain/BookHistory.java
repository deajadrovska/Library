package mk.finki.ukim.mk.library.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class BookHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Book book;

    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne
    private Author author;

    private Integer availableCopies;

    private LocalDateTime modifiedAt;

    @ManyToOne
    private User modifiedBy;

    public BookHistory() {
        this.modifiedAt = LocalDateTime.now();
    }

    public BookHistory(Book book, User modifiedBy) {
        this();
        this.book = book;
        this.name = book.getName();
        this.category = book.getCategory();
        this.author = book.getAuthor();
        this.availableCopies = book.getAvailableCopies();
        this.modifiedBy = modifiedBy;
    }

//    public Long getId() {
//        return id;
//    }
//
//    public Book getBook() {
//        return book;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public Category getCategory() {
//        return category;
//    }
//
//    public Author getAuthor() {
//        return author;
//    }
//
//    public Integer getAvailableCopies() {
//        return availableCopies;
//    }
//
//    public LocalDateTime getModifiedAt() {
//        return modifiedAt;
//    }
//
//    public User getModifiedBy() {
//        return modifiedBy;
//    }
}
