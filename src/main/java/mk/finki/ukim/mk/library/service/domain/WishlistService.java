package mk.finki.ukim.mk.library.service.domain;

import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.domain.Wishlist;

import java.util.List;
import java.util.Optional;

public interface WishlistService {

    Optional<Wishlist> getActiveWishlist(String username);

    Optional<Wishlist> addBookToWishlist(String username, Long bookId);

    Optional<Wishlist> removeBookFromWishlist(String username, Long bookId);

    Optional<Wishlist> borrowAllBooks(String username);

    Optional<Wishlist> createWishlist(User user);

    List<Book> listBooksInWishlist(String username);
}
