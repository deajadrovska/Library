package mk.finki.ukim.mk.library.model.Dto;

import lombok.Data;
import mk.finki.ukim.mk.library.model.Category;

@Data
public class BookDto {

    private String name;

    private Category category;

    private Long authorId;

    private Integer availableCopies;

    public BookDto() {
    }

    public BookDto(String name, Category category, Long authorId, Integer availableCopies) {
        this.name = name;
        this.category = category;
        this.authorId = authorId;
        this.availableCopies = availableCopies;
    }

//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setCategory(Category category) {
//        this.category = category;
//    }
//
//    public void setAuthorId(Long authorId) {
//        this.authorId = authorId;
//    }
//
//    public void setAvailableCopies(Integer availableCopies) {
//        this.availableCopies = availableCopies;
//    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }
}
