package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.Wishlist;
import mk.finki.ukim.mk.library.model.enumerations.WishlistStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record DisplayWishlistDto(
        Long id,
        String username,
        List<DisplayBookDto> books,
        LocalDateTime dateCreated,
        WishlistStatus status
) {
    public static DisplayWishlistDto from(Wishlist wishlist) {
        return new DisplayWishlistDto(
                wishlist.getId(),
                wishlist.getUser().getUsername(),
                wishlist.getBooks().stream().map(DisplayBookDto::from).collect(Collectors.toList()),
                wishlist.getDateCreated(),
                wishlist.getStatus()
        );
    }
}