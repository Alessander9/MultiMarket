package com.multimarket.repositories;

import com.multimarket.models.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByPedidoId(Long pedidoId);
    Optional<Pago> findByCodigoOperacion(String codigoOperacion);
    List<Pago> findByPedidoVendedorUsuarioCorreoOrderByFechaPagoDesc(String correo);
}
