package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.domain.Wishlist;
import mk.finki.ukim.mk.library.model.enumerations.WishlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserAndStatus(User user, WishlistStatus status);
}
