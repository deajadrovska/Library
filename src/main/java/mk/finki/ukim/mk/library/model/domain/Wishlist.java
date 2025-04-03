package mk.finki.ukim.mk.library.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import mk.finki.ukim.mk.library.model.enumerations.WishlistStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    @ManyToMany
    private List<Book> books;

    private LocalDateTime dateCreated;

    @Enumerated(EnumType.STRING)
    private WishlistStatus status;

    public Wishlist() {
        this.books = new ArrayList<>();
        this.dateCreated = LocalDateTime.now();
        this.status = WishlistStatus.CREATED;
    }

    public Wishlist(User user) {
        this();
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<Book> getBooks() {
        return books;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public WishlistStatus getStatus() {
        return status;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setStatus(WishlistStatus status) {
        this.status = status;
    }
}