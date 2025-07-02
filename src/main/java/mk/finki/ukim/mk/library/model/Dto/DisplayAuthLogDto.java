package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.AuthenticationLog;

import java.time.LocalDateTime;



public record DisplayAuthLogDto(
        String username,
        String token,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt
) {
    public static DisplayAuthLogDto from(AuthenticationLog log) {
        return new DisplayAuthLogDto(
                log.getUsername(),
                log.getToken(),
                log.getIssuedAt(),
                log.getExpiresAt()
        );
    }
}
