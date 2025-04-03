package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.BookHistory;
import mk.finki.ukim.mk.library.model.domain.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record DisplayBookHistoryDto(
        Long id,
        Long bookId,
        String name,
        Category category,
        String authorName,
        Integer availableCopies,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public static DisplayBookHistoryDto from(BookHistory history) {
        return new DisplayBookHistoryDto(
                history.getId(),
                history.getBook().getId(),
                history.getName(),
                history.getCategory(),
                history.getAuthor().getName() + " " + history.getAuthor().getSurname(),
                history.getAvailableCopies(),
                history.getModifiedAt(),
                history.getModifiedBy().getUsername()
        );
    }

    public static List<DisplayBookHistoryDto> from(List<BookHistory> histories) {
        return histories.stream()
                .map(DisplayBookHistoryDto::from)
                .collect(Collectors.toList());
    }
}
