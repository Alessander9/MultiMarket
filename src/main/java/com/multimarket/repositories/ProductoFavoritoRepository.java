package com.multimarket.repositories;

import com.multimarket.models.ProductoFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoFavoritoRepository extends JpaRepository<ProductoFavorito, Long> {
    Optional<ProductoFavorito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
    List<ProductoFavorito> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
}
