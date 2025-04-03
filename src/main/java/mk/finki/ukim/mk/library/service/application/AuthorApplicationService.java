package mk.finki.ukim.mk.library.service.application;

import mk.finki.ukim.mk.library.model.Dto.CreateAuthorDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthorDto;

import java.util.List;
import java.util.Optional;

public interface AuthorApplicationService {
    List<DisplayAuthorDto> findAll();
    Optional<DisplayAuthorDto> findById(Long id);
    Optional<DisplayAuthorDto> save(CreateAuthorDto authorDto);
    Optional<DisplayAuthorDto> update(Long id, CreateAuthorDto authorDto);
    void deleteById(Long id);
}
