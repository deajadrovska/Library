package mk.finki.ukim.mk.library.service.domain.Impl;

import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.domain.Wishlist;
import mk.finki.ukim.mk.library.model.enumerations.WishlistStatus;
import mk.finki.ukim.mk.library.repository.WishlistRepository;
import mk.finki.ukim.mk.library.service.domain.BookService;
import mk.finki.ukim.mk.library.service.domain.UserService;
import mk.finki.ukim.mk.library.service.domain.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;
    private final BookService bookService;

    public WishlistServiceImpl(
            WishlistRepository wishlistRepository,
            UserService userService,
            BookService bookService
    ) {
        this.wishlistRepository = wishlistRepository;
        this.userService = userService;
        this.bookService = bookService;
    }

//    @Override
//    public Optional<Wishlist> getActiveWishlist(String username) {
//        User user = userService.findByUsername(username);
////                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return wishlistRepository.findByUserAndStatus(user, WishlistStatus.CREATED)
//                .or(() -> createWishlist(user));
//    }
    @Override
    public Optional<Wishlist> getActiveWishlist(String username) {
        User user = userService.findByUsername(username);
        // If user is null, throw exception
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return wishlistRepository.findByUserAndStatus(user, WishlistStatus.CREATED)
                .or(() -> createWishlist(user));
    }

    @Override
    public Optional<Wishlist> addBookToWishlist(String username, Long bookId) {
        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No available copies for this book");
        }

        return getActiveWishlist(username)
                .map(wishlist -> {
                    List<Book> books = wishlist.getBooks();
                    if (!books.contains(book)) {
                        books.add(book);
                    }
                    return wishlistRepository.save(wishlist);
                });
    }

    @Override
    public Optional<Wishlist> removeBookFromWishlist(String username, Long bookId) {
        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return getActiveWishlist(username)
                .map(wishlist -> {
                    wishlist.getBooks().removeIf(b -> b.getId().equals(bookId));
                    return wishlistRepository.save(wishlist);
                });
    }

    @Override
    @Transactional
    public Optional<Wishlist> borrowAllBooks(String username) {
        return getActiveWishlist(username)
                .map(wishlist -> {
                    List<Book> books = new ArrayList<>(wishlist.getBooks());
                    for (Book book : books) {
                        if (book.getAvailableCopies() > 0) {
                            bookService.markAsBorrowed(book.getId());
                        } else {
                            throw new RuntimeException("Not enough copies available for book: " + book.getName());
                        }
                    }
//                    wishlist.setStatus(WishlistStatus.BORROWED);
                    wishlist.getBooks().clear();
                    return wishlistRepository.save(wishlist);
                });
    }

    @Override
    public Optional<Wishlist> createWishlist(User user) {
        Wishlist wishlist = new Wishlist(user);
        return Optional.of(wishlistRepository.save(wishlist));
    }

    @Override
    public List<Book> listBooksInWishlist(String username) {
        return getActiveWishlist(username)
                .map(Wishlist::getBooks)
                .orElse(new ArrayList<>());
    }
}
