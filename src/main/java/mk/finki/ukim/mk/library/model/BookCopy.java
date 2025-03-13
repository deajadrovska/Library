package mk.finki.ukim.mk.library.model;

import jakarta.persistence.*;
import lombok.Data;
import mk.finki.ukim.mk.library.model.enumeration.BookCondition;
import jakarta.persistence.*;
@Data
@Entity
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Book book;

    private Boolean borrowed;

    @Enumerated(EnumType.STRING)
    private BookCondition condition;

    public BookCopy() {
        this.borrowed = false;
    }

    public BookCopy(Book book, BookCondition condition) {
        this();
        this.book = book;
        this.condition = condition;
    }

    public BookCopy(Book book, Boolean borrowed, BookCondition condition) {
        this.book = book;
        this.borrowed = borrowed;
        this.condition = condition;
    }

    public Book getBook() {
        return book;
    }

    public Boolean getBorrowed() {
        return borrowed;
    }

    public BookCondition getCondition() {
        return condition;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setBorrowed(Boolean borrowed) {
        this.borrowed = borrowed;
    }

    public void setCondition(BookCondition condition) {
        this.condition = condition;
    }
}
