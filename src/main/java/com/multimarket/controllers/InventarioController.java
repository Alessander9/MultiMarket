package com.multimarket.controllers;

import com.multimarket.dto.ActualizarStockRequest;
import com.multimarket.dto.InventarioResponse;
import com.multimarket.dto.MovimientoInventarioRequest;
import com.multimarket.dto.MovimientoInventarioResponse;
import com.multimarket.models.Producto;
import com.multimarket.repositories.ProductoRepository;
import com.multimarket.services.Interfaces.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventarios")
public class InventarioController {

    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;

    public InventarioController(InventarioService inventarioService, ProductoRepository productoRepository) {
        this.inventarioService = inventarioService;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/productos/{productoId}")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    public ResponseEntity<InventarioResponse> consultarStock(
            @PathVariable Long productoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Si el usuario es un Vendedor, validar que el producto sea suyo
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
            validarPropiedadProducto(productoId, userDetails.getUsername());
        }
        
        InventarioResponse response = inventarioService.consultarStock(productoId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/productos/{productoId}/stock-minimo")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<InventarioResponse> actualizarStockMinimo(
            @PathVariable Long productoId,
            @Valid @RequestBody ActualizarStockRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        validarPropiedadProducto(productoId, userDetails.getUsername());
        
        InventarioResponse response = inventarioService.actualizarStockMinimo(productoId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/productos/{productoId}/movimientos")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<InventarioResponse> registrarMovimiento(
            @PathVariable Long productoId,
            @Valid @RequestBody MovimientoInventarioRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        validarPropiedadProducto(productoId, userDetails.getUsername());
        
        InventarioResponse response = inventarioService.registrarMovimiento(productoId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/productos/{productoId}/movimientos")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    public ResponseEntity<List<MovimientoInventarioResponse>> obtenerHistorial(
            @PathVariable Long productoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
            validarPropiedadProducto(productoId, userDetails.getUsername());
        }
        
        List<MovimientoInventarioResponse> response = inventarioService.obtenerHistorial(productoId);
        return ResponseEntity.ok(response);
    }

    // Método utilitario para validar propiedad del producto
    private void validarPropiedadProducto(Long productoId, String correo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con el ID: " + productoId));
        
        if (!producto.getVendedor().getUsuario().getCorreo().equals(correo)) {
            throw new SecurityException("Acceso Denegado: No tienes permisos para gestionar el inventario de un producto que no pertenece a tu tienda.");
        }
    }
}
