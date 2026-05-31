package com.multimarket.repositories;

import com.multimarket.models.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {
    List<SesionUsuario> findByUsuarioIdAndActiva(Long usuarioId, boolean activa);
}
