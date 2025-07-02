package mk.finki.ukim.mk.library.service.domain;

import mk.finki.ukim.mk.library.model.Dto.DisplayAuthLogDto;
import mk.finki.ukim.mk.library.model.domain.AuthenticationLog;

import java.util.List;

public interface AuthenticationLogService {
    List<AuthenticationLog> findAllAuthenticationLogs();

    void saveAuthenticationLog(AuthenticationLog authenticationLog);
}
