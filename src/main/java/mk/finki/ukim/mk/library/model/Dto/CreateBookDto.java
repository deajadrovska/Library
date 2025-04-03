package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.Author;
import mk.finki.ukim.mk.library.model.domain.Book;
import mk.finki.ukim.mk.library.model.domain.Category;

public record CreateBookDto(
        String name,
        Category category,
        Long authorId,
        Integer availableCopies
) {
    public Book toBook(Author author) {
        Book book = new Book();
        book.setName(name);
        book.setCategory(category);
        book.setAuthor(author);
        book.setAvailableCopies(availableCopies);
        return book;
    }
}
