package com.multimarket.repositories;

import com.multimarket.models.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    List<Conversacion> findByCompradorCorreoOrderByFechaCreacionDesc(String compradorCorreo);
    List<Conversacion> findByVendedorUsuarioCorreoOrderByFechaCreacionDesc(String vendedorCorreo);
    Optional<Conversacion> findByCompradorIdAndVendedorId(Long compradorId, Long vendedorId);
}
