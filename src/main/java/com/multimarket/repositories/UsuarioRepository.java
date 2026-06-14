package com.multimarket.repositories;

import com.multimarket.models.RolNombre;
import com.multimarket.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);

    @Query("select distinct u from Usuario u join u.roles r where r.nombre = :nombre")
    List<Usuario> findAllByRoleNombre(@Param("nombre") RolNombre nombre);
}
