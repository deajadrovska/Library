package mk.finki.ukim.mk.library.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String name;
    private String surname;

    @ManyToOne
    private Country country;

    public Author() {
    }

    public Author(String name, String surname, Country country) {
        this.name = name;
        this.surname = surname;
        this.country = country;
    }
}
