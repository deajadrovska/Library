package mk.finki.ukim.mk.library.service.domain;

import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {
    User register(String username, String password, String repeatPassword, String name, String surname, Role role);

    User login(String username, String password);

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    User findByUsername(String username);
}
