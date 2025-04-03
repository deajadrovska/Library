package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Category;

import java.util.List;
import java.util.stream.Collectors;

public record DisplayBookDto(
        Long id,
        String name,
        Category category,
        DisplayAuthorDto author,
        Integer availableCopies
) {
    public static DisplayBookDto from(Book book) {
        return new DisplayBookDto(
                book.getId(),
                book.getName(),
                book.getCategory(),
                DisplayAuthorDto.from(book.getAuthor()),
                book.getAvailableCopies()
        );
    }

    public static List<DisplayBookDto> from(List<Book> books) {
        return books.stream()
                .map(DisplayBookDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Category category() {
        return category;
    }

    @Override
    public DisplayAuthorDto author() {
        return author;
    }

    @Override
    public Integer availableCopies() {
        return availableCopies;
    }
}
