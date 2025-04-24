package mk.finki.ukim.mk.library.repository;

import mk.finki.ukim.mk.library.model.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsernameAndPassword(String username, String password);

    // Add entity graph to avoid fetching the wishlist when loading a user
    @EntityGraph(attributePaths = {})
    Optional<User> findByUsername(String username);

    // Add entity graph to the findAll method to avoid fetching wishlists
    @EntityGraph(attributePaths = {})
    @Override
    List<User> findAll();


}