package com.multimarket.repositories;

import com.multimarket.models.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByConversacionIdOrderByFechaEnvioAsc(Long conversacionId);
    Optional<Mensaje> findTopByConversacionIdOrderByFechaEnvioDesc(Long conversacionId);
    long countByConversacionIdAndLeidoFalseAndRemitenteIdNot(Long conversacionId, Long remitenteId);
}
