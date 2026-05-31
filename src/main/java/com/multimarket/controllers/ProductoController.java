package com.multimarket.controllers;

import com.multimarket.dto.ImagenProductoResponse;
import com.multimarket.dto.ProductoRequest;
import com.multimarket.dto.ProductoResponse;
import com.multimarket.services.Interfaces.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestBody ProductoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductoResponse response = productoService.crearProducto(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoResponse> editarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductoResponse response = productoService.editarProducto(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> consultarProducto(@PathVariable Long id) {
        ProductoResponse response = productoService.consultarProducto(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarProductosActivos() {
        List<ProductoResponse> response = productoService.listarProductosActivos();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Void> desactivarProducto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productoService.desactivarProducto(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Búsqueda avanzada
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) BigDecimal minPrecio,
            @RequestParam(required = false) BigDecimal maxPrecio) {
        List<ProductoResponse> response = productoService.buscarProductos(nombre, categoriaId, vendedorId, minPrecio, maxPrecio);
        return ResponseEntity.ok(response);
    }

    // CRUD Imágenes
    @PostMapping("/{id}/imagenes")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ImagenProductoResponse> agregarImagen(
            @PathVariable Long id,
            @RequestParam String url,
            @RequestParam(defaultValue = "false") boolean principal,
            @RequestParam(defaultValue = "0") int orden,
            @AuthenticationPrincipal UserDetails userDetails) {
        ImagenProductoResponse response = productoService.agregarImagen(id, userDetails.getUsername(), url, principal, orden);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/imagenes/{imagenId}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Long imagenId,
            @AuthenticationPrincipal UserDetails userDetails) {
        productoService.eliminarImagen(imagenId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Favoritos
    @PostMapping("/favoritos/{productoId}")
    public ResponseEntity<String> agregarFavorito(
            @PathVariable Long productoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String result = productoService.agregarFavorito(userDetails.getUsername(), productoId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/favoritos/{productoId}")
    public ResponseEntity<String> eliminarFavorito(
            @PathVariable Long productoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String result = productoService.eliminarFavorito(userDetails.getUsername(), productoId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/favoritos")
    public ResponseEntity<List<ProductoResponse>> listarFavoritos(@AuthenticationPrincipal UserDetails userDetails) {
        List<ProductoResponse> response = productoService.listarFavoritos(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
