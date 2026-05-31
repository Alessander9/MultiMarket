package com.multimarket.controllers;

import com.multimarket.dto.CategoriaRequest;
import com.multimarket.dto.CategoriaResponse;
import com.multimarket.services.Interfaces.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.crearCategoria(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarCategoriasActivas() {
        List<CategoriaResponse> response = categoriaService.listarCategoriasActivas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> consultarCategoria(@PathVariable Long id) {
        CategoriaResponse response = categoriaService.consultarCategoria(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> editarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.editarCategoria(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivarCategoria(@PathVariable Long id) {
        categoriaService.desactivarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
