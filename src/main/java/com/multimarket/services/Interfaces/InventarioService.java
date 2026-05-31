package com.multimarket.services.Interfaces;

import com.multimarket.dto.ActualizarStockRequest;
import com.multimarket.dto.InventarioResponse;
import com.multimarket.dto.MovimientoInventarioRequest;
import com.multimarket.dto.MovimientoInventarioResponse;
import java.util.List;

public interface InventarioService {
    InventarioResponse consultarStock(Long productoId);
    InventarioResponse actualizarStockMinimo(Long productoId, ActualizarStockRequest request);
    InventarioResponse registrarMovimiento(Long productoId, MovimientoInventarioRequest request);
    List<MovimientoInventarioResponse> obtenerHistorial(Long productoId);
}
