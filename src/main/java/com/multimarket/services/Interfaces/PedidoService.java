package com.multimarket.services.Interfaces;

import com.multimarket.dto.ActualizarEstadoPedidoRequest;
import com.multimarket.dto.CompraAgrupadaRequest;
import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.dto.PedidoRequest;
import com.multimarket.dto.PedidoResponse;
import java.util.List;

public interface PedidoService {
    PedidoResponse crearPedido(String compradorCorreo, PedidoRequest request);
    CompraAgrupadaResponse crearPedidosAgrupados(String compradorCorreo, CompraAgrupadaRequest request);
    PedidoResponse consultarPedido(Long id);
    List<PedidoResponse> listarPedidosPorComprador(String compradorCorreo);
    List<PedidoResponse> listarPedidosPorVendedor(String vendedorCorreo);
    PedidoResponse cancelarPedido(Long id, String usuarioCorreo);
    PedidoResponse actualizarEstado(Long id, String vendedorCorreo, ActualizarEstadoPedidoRequest request);
}
