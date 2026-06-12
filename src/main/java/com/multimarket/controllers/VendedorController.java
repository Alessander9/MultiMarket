package com.multimarket.controllers;

import com.multimarket.dto.ProductoResponse;
import com.multimarket.dto.VendedorRequest;
import com.multimarket.dto.VendedorResponse;
import com.multimarket.services.Interfaces.ProductoService;
import com.multimarket.services.Interfaces.VendedorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendedores")
public class VendedorController {

    private final VendedorService vendedorService;
    private final ProductoService productoService;

    public VendedorController(VendedorService vendedorService, ProductoService productoService) {
        this.vendedorService = vendedorService;
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<VendedorResponse>> listarTodos() {
        List<VendedorResponse> response = vendedorService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    public ResponseEntity<VendedorResponse> crearTienda(
            @Valid @RequestBody VendedorRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String emailToUse = (request.getCorreoUsuario() != null && !request.getCorreoUsuario().trim().isEmpty() &&
                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                ? request.getCorreoUsuario()
                : userDetails.getUsername();

        if (emailToUse == null || emailToUse.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo del usuario vendedor es obligatorio para administradores.");
        }

        VendedorResponse response = vendedorService.crearTienda(emailToUse, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<VendedorResponse> editarTienda(
            @PathVariable Long id,
            @Valid @RequestBody VendedorRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        VendedorResponse response = vendedorService.editarTienda(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorResponse> consultarTienda(@PathVariable Long id) {
        VendedorResponse response = vendedorService.consultarTienda(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mi-tienda")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<VendedorResponse> consultarMiTienda(@AuthenticationPrincipal UserDetails userDetails) {
        VendedorResponse response = vendedorService.consultarMiTienda(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mi-tienda/productos")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<List<ProductoResponse>> listarProductosMiTienda(
            @AuthenticationPrincipal UserDetails userDetails) {
        VendedorResponse tienda = vendedorService.consultarMiTienda(userDetails.getUsername());
        List<ProductoResponse> response = productoService.listarProductosPorTienda(tienda.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoResponse>> listarProductosDeTienda(
            @PathVariable Long id) {
        List<ProductoResponse> response = productoService.listarProductosPorTienda(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<VendedorResponse> desactivarTienda(
            @PathVariable Long id,
            @RequestParam boolean activo,
            @AuthenticationPrincipal UserDetails userDetails) {
        VendedorResponse response = vendedorService.desactivarTienda(id, userDetails.getUsername(), activo);
        return ResponseEntity.ok(response);
    }
}
