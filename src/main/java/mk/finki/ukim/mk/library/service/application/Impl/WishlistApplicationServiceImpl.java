package mk.finki.ukim.mk.library.service.application.Impl;

import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayWishlistDto;
import mk.finki.ukim.mk.library.service.application.WishlistApplicationService;
import mk.finki.ukim.mk.library.service.domain.WishlistService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishlistApplicationServiceImpl implements WishlistApplicationService {

    private final WishlistService wishlistService;

    public WishlistApplicationServiceImpl(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @Override
    public Optional<DisplayWishlistDto> getActiveWishlist(String username) {
        return wishlistService.getActiveWishlist(username)
                .map(DisplayWishlistDto::from);
    }

    @Override
    public Optional<DisplayWishlistDto> addBookToWishlist(String username, Long bookId) {
        try {
            return wishlistService.addBookToWishlist(username, bookId)
                    .map(DisplayWishlistDto::from);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Optional<DisplayWishlistDto> removeBookFromWishlist(String username, Long bookId) {
        return wishlistService.removeBookFromWishlist(username, bookId)
                .map(DisplayWishlistDto::from);
    }

    @Override
    public Optional<DisplayWishlistDto> borrowAllBooks(String username) {
        try {
            return wishlistService.borrowAllBooks(username)
                    .map(DisplayWishlistDto::from);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<DisplayBookDto> listBooksInWishlist(String username) {
        return wishlistService.listBooksInWishlist(username)
                .stream()
                .map(DisplayBookDto::from)
                .collect(Collectors.toList());
    }
}
