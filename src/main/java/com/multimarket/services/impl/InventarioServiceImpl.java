package com.multimarket.services.impl;

import com.multimarket.dto.ActualizarStockRequest;
import com.multimarket.dto.InventarioResponse;
import com.multimarket.dto.MovimientoInventarioRequest;
import com.multimarket.dto.MovimientoInventarioResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.InventarioRepository;
import com.multimarket.repositories.MovimientoInventarioRepository;
import com.multimarket.repositories.ProductoRepository;
import com.multimarket.services.Interfaces.InventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioServiceImpl(InventarioRepository inventarioRepository,
                                 MovimientoInventarioRepository movimientoInventarioRepository,
                                 ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponse consultarStock(Long productoId) {
        Inventario inventario = obtenerOCrearInventario(productoId);
        return mapToResponse(inventario);
    }

    @Override
    public InventarioResponse actualizarStockMinimo(Long productoId, ActualizarStockRequest request) {
        Inventario inventario = obtenerOCrearInventario(productoId);
        inventario.setStockMinimo(request.getStockMinimo());
        inventario.setUltimaActualizacion(LocalDateTime.now());
        Inventario saved = inventarioRepository.save(inventario);
        return mapToResponse(saved);
    }

    @Override
    public InventarioResponse registrarMovimiento(Long productoId, MovimientoInventarioRequest request) {
        Inventario inventario = obtenerOCrearInventario(productoId);
        Producto producto = inventario.getProducto();

        int cantidad = request.getCantidad();
        int stockAnterior = inventario.getStockActual();
        int nuevoStock = stockAnterior;

        switch (request.getTipoMovimiento()) {
            case ENTRADA:
            case DEVOLUCION:
                nuevoStock += cantidad;
                break;
            case SALIDA:
                if (stockAnterior < cantidad) {
                    throw new IllegalArgumentException("Stock insuficiente para realizar el despacho. Stock actual: " + stockAnterior + ", Cantidad solicitada: " + cantidad);
                }
                nuevoStock -= cantidad;
                break;
            case AJUSTE:
                // Un ajuste establece el stock actual según el conteo físico real
                nuevoStock = cantidad;
                break;
        }

        // Actualizar inventario
        inventario.setStockActual(nuevoStock);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        Inventario savedInventario = inventarioRepository.save(inventario);

        // Sincronizar campo stock en la entidad Producto para búsquedas y caché de lectura
        producto.setStock(nuevoStock);
        producto.setFechaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);

        // Guardar movimiento de auditoría
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(request.getTipoMovimiento());
        movimiento.setCantidad(cantidad);
        movimiento.setObservacion(request.getObservacion());
        movimiento.setInventario(savedInventario);
        movimientoInventarioRepository.save(movimiento);

        return mapToResponse(savedInventario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> obtenerHistorial(Long productoId) {
        Inventario inventario = obtenerOCrearInventario(productoId);
        List<MovimientoInventario> movimientos = movimientoInventarioRepository
                .findByInventarioIdOrderByFechaMovimientoDesc(inventario.getId());

        return movimientos.stream()
                .map(m -> new MovimientoInventarioResponse(
                        m.getId(),
                        m.getTipoMovimiento().name(),
                        m.getCantidad(),
                        m.getObservacion(),
                        m.getFechaMovimiento()
                ))
                .collect(Collectors.toList());
    }

    // Helper para garantizar la existencia de un registro de inventario (Self-healing)
    private Inventario obtenerOCrearInventario(Long productoId) {
        return inventarioRepository.findByProductoId(productoId)
                .orElseGet(() -> {
                    Producto producto = productoRepository.findById(productoId)
                            .orElseThrow(() -> new IllegalArgumentException("No existe un producto con el ID especificado: " + productoId));
                    
                    Inventario nuevo = new Inventario();
                    // Tomamos el stock inicial del producto si ya lo tenía
                    nuevo.setStockActual(producto.getStock());
                    nuevo.setStockMinimo(0);
                    nuevo.setProducto(producto);
                    nuevo.setUltimaActualizacion(LocalDateTime.now());
                    
                    return inventarioRepository.save(nuevo);
                });
    }

    private InventarioResponse mapToResponse(Inventario inv) {
        return new InventarioResponse(
                inv.getId(),
                inv.getStockActual(),
                inv.getStockMinimo(),
                inv.getUltimaActualizacion(),
                inv.getProducto().getId(),
                inv.getProducto().getNombre()
        );
    }
}
