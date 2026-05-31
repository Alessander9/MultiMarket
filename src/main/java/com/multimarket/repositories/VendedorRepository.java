package com.multimarket.repositories;

import com.multimarket.models.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Optional<Vendedor> findByUsuarioId(Long usuarioId);
    Optional<Vendedor> findByNombreTienda(String nombreTienda);
    Optional<Vendedor> findByUsuarioCorreo(String correo);
}
