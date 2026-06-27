package com.multimarket.controllers;

import com.multimarket.dto.ActualizarEstadoPedidoRequest;
import com.multimarket.dto.CompraAgrupadaRequest;
import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.dto.PedidoRequest;
import com.multimarket.dto.PedidoResponse;
import com.multimarket.services.Interfaces.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(
            @Valid @RequestBody PedidoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoResponse response = pedidoService.crearPedido(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/agrupados")
    public ResponseEntity<CompraAgrupadaResponse> crearPedidosAgrupados(
            @Valid @RequestBody CompraAgrupadaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CompraAgrupadaResponse response = pedidoService.crearPedidosAgrupados(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> consultarPedido(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoResponse response = pedidoService.consultarPedido(id);
        
        // Regla de seguridad: solo el comprador, el vendedor o un administrador pueden ver el detalle
        boolean esComprador = response.getCompradorCorreo().equals(userDetails.getUsername());
        boolean esVendedor = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));
        boolean esAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!esComprador && !esVendedor && !esAdmin) {
            throw new SecurityException("Acceso Denegado: No tienes permisos para ver este pedido.");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorComprador(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PedidoResponse> response = pedidoService.listarPedidosPorComprador(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tienda")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorVendedor(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PedidoResponse> response = pedidoService.listarPedidosPorVendedor(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelarPedido(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoResponse response = pedidoService.cancelarPedido(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<PedidoResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoPedidoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PedidoResponse response = pedidoService.actualizarEstado(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
