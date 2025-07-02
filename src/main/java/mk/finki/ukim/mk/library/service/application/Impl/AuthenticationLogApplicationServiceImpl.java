package mk.finki.ukim.mk.library.service.application.Impl;

import mk.finki.ukim.mk.library.model.Dto.DisplayAuthLogDto;
import mk.finki.ukim.mk.library.model.domain.AuthenticationLog;
import mk.finki.ukim.mk.library.service.application.AuthenticationLogApplicationService;
import mk.finki.ukim.mk.library.service.domain.AuthenticationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationLogApplicationServiceImpl implements AuthenticationLogApplicationService {

    private final AuthenticationLogService authenticationLogService;

    public AuthenticationLogApplicationServiceImpl(AuthenticationLogService authenticationLogService) {
        this.authenticationLogService = authenticationLogService;
    }

    @Override
    public List<DisplayAuthLogDto> getAllAuthenticationLogs() {
        List<AuthenticationLog> logs = authenticationLogService.findAllAuthenticationLogs();
        return logs.stream()
                .map(DisplayAuthLogDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public void createAuthenticationLog(String username, String token, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        AuthenticationLog authenticationLog = new AuthenticationLog(username, token, issuedAt, expiresAt);
        authenticationLogService.saveAuthenticationLog(authenticationLog);
    }
}
