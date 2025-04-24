package mk.finki.ukim.mk.library.config;

import jakarta.annotation.PostConstruct;
import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Country;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.repository.AuthorRepository;
import mk.finki.ukim.mk.library.repository.BookRepository;
import mk.finki.ukim.mk.library.repository.CountryRepository;
import mk.finki.ukim.mk.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import mk.finki.ukim.mk.library.model.enumerations.Role;

@Component
public class DataInitializer {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AuthorRepository authorRepository,
            BookRepository bookRepository,
            CountryRepository countryRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.countryRepository = countryRepository;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

//    @PostConstruct
    public void init() {
        countryRepository.save(new Country("MKD", "EUROPE"));
        countryRepository.save(new Country("UK", "EUROPE"));



        userRepository.save(new User(
                "dj",
                passwordEncoder.encode("dj"),
                "Dea",
                "Jadrovska",
                Role.ROLE_LIBRARIAN
        ));
    }
}
