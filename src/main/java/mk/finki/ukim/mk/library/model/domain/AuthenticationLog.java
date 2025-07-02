package mk.finki.ukim.mk.library.model.domain;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MapKey;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class AuthenticationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String username;

    @Column(length = 2000)
    private String token;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    public AuthenticationLog(String username, String token, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.username = username;
        this.token = token;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
