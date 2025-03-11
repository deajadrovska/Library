package mk.finki.ukim.mk.library.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

//@Data
//package mk.finki.ukim.mk.library.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Data
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // lowercase "id" to match getter/setter

    private String name;
    private String continent;

    public Country() {
    }

    public Country(String name, String continent) {
        this.name = name;
        this.continent = continent;
    }

    public String getName() {
        return name;
    }

    public String getContinent() {
        return continent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    //    public void setId(Long id) {
//        this.id = id; // lowercase "id"
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setContinent(String continent) {
//        this.continent = continent;
//    }
//
//    public Long getId() {
//        return id; // lowercase "id"
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getContinent() {
//        return continent;
//    }
}
