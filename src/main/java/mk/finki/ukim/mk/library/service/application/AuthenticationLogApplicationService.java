package mk.finki.ukim.mk.library.service.application;

import mk.finki.ukim.mk.library.model.Dto.DisplayAuthLogDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AuthenticationLogApplicationService {
    List<DisplayAuthLogDto> getAllAuthenticationLogs();

    void createAuthenticationLog(String username, String token, LocalDateTime issuedAt, LocalDateTime expiresAt);


}
