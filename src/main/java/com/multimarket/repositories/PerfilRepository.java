package com.multimarket.repositories;

import com.multimarket.models.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    boolean existsByDni(String dni);
}
