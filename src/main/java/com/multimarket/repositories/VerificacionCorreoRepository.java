package com.multimarket.repositories;

import com.multimarket.models.VerificacionCorreo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificacionCorreoRepository extends JpaRepository<VerificacionCorreo, Long> {
    Optional<VerificacionCorreo> findByToken(UUID token);
}
