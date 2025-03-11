package mk.finki.ukim.mk.library.model.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AuthorDto {
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setSurname(String surname) {
//        this.surname = surname;
//    }
//
//    public void setCountryId(Long countryId) {
//        this.countryId = countryId;
//    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Long getCountryId() {
        return countryId;
    }

    private String name;

    private String surname;

    private Long countryId;

    public AuthorDto() {
    }

    public AuthorDto(String name, String surname, Long countryId) {
        this.name = name;
        this.surname = surname;
        this.countryId = countryId;
    }
}
