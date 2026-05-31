package com.multimarket.controllers;

import com.multimarket.dto.VendedorRequest;
import com.multimarket.dto.VendedorResponse;
import com.multimarket.services.Interfaces.VendedorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedores")
public class VendedorController {

    private final VendedorService vendedorService;

    public VendedorController(VendedorService vendedorService) {
        this.vendedorService = vendedorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<VendedorResponse> crearTienda(
            @Valid @RequestBody VendedorRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        VendedorResponse response = vendedorService.crearTienda(userDetails.getUsername(), request);
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

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<VendedorResponse> desactivarTienda(
            @PathVariable Long id,
            @RequestParam boolean activo,
            @AuthenticationPrincipal UserDetails userDetails) {
        VendedorResponse response = vendedorService.desactivarTienda(id, userDetails.getUsername(), activo);
        return ResponseEntity.ok(response);
    }
}
