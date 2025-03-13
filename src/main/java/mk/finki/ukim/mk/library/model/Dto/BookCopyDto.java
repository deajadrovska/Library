package mk.finki.ukim.mk.library.model.Dto;

import lombok.Data;
import mk.finki.ukim.mk.library.model.enumeration.BookCondition;


@Data
public class BookCopyDto {

    private Long bookId;

    private BookCondition condition;

    public BookCopyDto() {
    }

    public BookCopyDto(Long bookId, BookCondition condition) {
        this.bookId = bookId;
        this.condition = condition;
    }

    public BookCondition getCondition() {
        return condition;
    }

    public void setCondition(BookCondition condition) {
        this.condition = condition;
    }

    public Long getBookId() {
        return bookId;
    }
}
