package com.multimarket.repositories;

import com.multimarket.models.CompraAgrupada;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompraAgrupadaRepository extends JpaRepository<CompraAgrupada, Long> {

    @EntityGraph(attributePaths = {"pedidos", "pedidos.vendedor", "comprador"})
    List<CompraAgrupada> findByCompradorCorreoOrderByFechaCompraDesc(String compradorCorreo);

    @EntityGraph(attributePaths = {"pedidos", "pedidos.vendedor", "comprador"})
    Optional<CompraAgrupada> findById(Long id);
}
