package com.multimarket.repositories;

import com.multimarket.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCompradorCorreoOrderByFechaPedidoDesc(String correo);
    List<Pedido> findByVendedorIdOrderByFechaPedidoDesc(Long vendedorId);
}
