package com.multimarket.repositories;

import com.multimarket.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioCorreoOrderByFechaCreacionDesc(String correo);
    Optional<Notificacion> findByIdAndUsuarioCorreo(Long id, String correo);
}
