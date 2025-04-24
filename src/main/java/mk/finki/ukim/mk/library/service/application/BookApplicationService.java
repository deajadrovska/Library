package mk.finki.ukim.mk.library.service.application;

import mk.finki.ukim.mk.library.model.Dto.CreateBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayBookHistoryDto;
import mk.finki.ukim.mk.library.model.domain.Category;

import java.util.List;
import java.util.Optional;

public interface BookApplicationService {
    List<DisplayBookDto> findAll();
    Optional<DisplayBookDto> findById(Long id);
    Optional<DisplayBookDto> save(CreateBookDto bookDto);
    Optional<DisplayBookDto> update(Long id, CreateBookDto bookDto);
    void deleteById(Long id);
    Optional<DisplayBookDto> markAsBorrowed(Long id);
    List<Category> findAllCategories();


    //new method
    List<DisplayBookHistoryDto> getBookHistory(Long bookId);
}
