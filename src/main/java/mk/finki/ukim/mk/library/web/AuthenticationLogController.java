package mk.finki.ukim.mk.library.web;


import lombok.RequiredArgsConstructor;
import mk.finki.ukim.mk.library.model.Dto.DisplayAuthLogDto;
import mk.finki.ukim.mk.library.service.application.AuthenticationLogApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth-logs")
@RequiredArgsConstructor
public class AuthenticationLogController {

    private final AuthenticationLogApplicationService authenticationLogApplicationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisplayAuthLogDto>> getAllAuthenticationLogs() {
        List<DisplayAuthLogDto> logs = authenticationLogApplicationService.getAllAuthenticationLogs();
        return ResponseEntity.ok(logs);
    }
}
