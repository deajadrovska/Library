package mk.finki.ukim.mk.library.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne
    private Author author;

//    private Integer availableCopies;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookCopy> copies = new ArrayList<BookCopy>();

    public Book() {
    }

    public Book(String name, Category category, Author author, List<BookCopy> copies) {
        this.name = name;
        this.category = category;
        this.author = author;
        this.copies = copies;
    }

    public Book(String name, Category category, Author author, Integer availableCopies) {
        this.name = name;
        this.category = category;
        this.author = author;
//        this.availableCopies = availableCopies;
    }

    public Book(String name, Category category, Author author) {
        this.name = name;
        this.category = category;
        this.author = author;
    }

//    public void setId(Long id) {
//        this.id = id;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

//    public void setAvailableCopies(Integer availableCopies) {
//        this.availableCopies = availableCopies;
//    }

//    public Long getId() {
//        return id;
//    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public Author getAuthor() {
        return author;
    }

//    public Integer getAvailableCopies() {
//        return availableCopies;
//    }
}
