package mk.finki.ukim.mk.library.service.application;

import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayWishlistDto;

import java.util.List;
import java.util.Optional;

public interface WishlistApplicationService {

    Optional<DisplayWishlistDto> getActiveWishlist(String username);

    Optional<DisplayWishlistDto> addBookToWishlist(String username, Long bookId);

    Optional<DisplayWishlistDto> removeBookFromWishlist(String username, Long bookId);

    Optional<DisplayWishlistDto> borrowAllBooks(String username);

    List<DisplayBookDto> listBooksInWishlist(String username);
}
