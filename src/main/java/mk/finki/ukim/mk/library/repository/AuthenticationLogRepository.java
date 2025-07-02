package mk.finki.ukim.mk.library.repository;


import mk.finki.ukim.mk.library.model.domain.AuthenticationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthenticationLogRepository extends JpaRepository<AuthenticationLog, Long> {
        List<AuthenticationLog> findByUsername(String username);
        List<AuthenticationLog> findAllByOrderByIssuedAtDesc();


}
