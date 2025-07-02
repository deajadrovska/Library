package mk.finki.ukim.mk.library.service.domain.Impl;

import mk.finki.ukim.mk.library.model.domain.AuthenticationLog;
import mk.finki.ukim.mk.library.repository.AuthenticationLogRepository;
import mk.finki.ukim.mk.library.service.domain.AuthenticationLogService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AuthenticationLogServiceImpl implements AuthenticationLogService {

    private final AuthenticationLogRepository authenticationLogRepository;

    public AuthenticationLogServiceImpl(AuthenticationLogRepository authenticationLogRepository) {
        this.authenticationLogRepository = authenticationLogRepository;
    }

    @Override
    public List<AuthenticationLog> findAllAuthenticationLogs() {
        return authenticationLogRepository.findAllByOrderByIssuedAtDesc();
    }

    @Override
    public void saveAuthenticationLog(AuthenticationLog authenticationLog) {
        authenticationLogRepository.save(authenticationLog);
    }
}
