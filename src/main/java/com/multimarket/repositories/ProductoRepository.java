package com.multimarket.repositories;

import com.multimarket.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySku(String sku);
    List<Producto> findByActivoTrue();
    List<Producto> findByVendedorUsuarioIdOrderByIdDesc(Long usuarioId);
    List<Producto> findByVendedorIdOrderByIdDesc(Long vendedorId);
    Optional<Producto> findByIdAndVendedorUsuarioId(Long id, Long usuarioId);

    @Query("SELECT p FROM Producto p WHERE p.activo = true " +
           "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId) " +
           "AND (:vendedorId IS NULL OR p.vendedor.id = :vendedorId) " +
           "AND (:minPrecio IS NULL OR p.precio >= :minPrecio) " +
           "AND (:maxPrecio IS NULL OR p.precio <= :maxPrecio)")
    List<Producto> searchProducts(
            @Param("nombre") String nombre,
            @Param("categoriaId") Long categoriaId,
            @Param("vendedorId") Long vendedorId,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio
    );
}
