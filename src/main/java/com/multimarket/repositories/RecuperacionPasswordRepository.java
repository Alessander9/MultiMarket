package com.multimarket.repositories;

import com.multimarket.models.RecuperacionPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecuperacionPasswordRepository extends JpaRepository<RecuperacionPassword, Long> {
    Optional<RecuperacionPassword> findByToken(UUID token);
}
