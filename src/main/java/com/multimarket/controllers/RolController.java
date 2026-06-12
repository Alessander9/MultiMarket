package com.multimarket.controllers;

import com.multimarket.dto.RoleResponse;
import com.multimarket.models.Rol;
import com.multimarket.models.RolNombre;
import com.multimarket.repositories.RolRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolController {

    private final RolRepository rolRepository;

    public RolController(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponse>> listarRoles() {
        List<RoleResponse> response = rolRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private RoleResponse toResponse(Rol rol) {
        List<String> permisos = switch (rol.getNombre()) {
            case ADMIN -> List.of("read", "write", "delete", "admin");
            case VENDEDOR -> List.of("read", "write");
            case COMPRADOR -> List.of("read");
        };
        return new RoleResponse(rol.getId(), rol.getNombre().name(), rol.getDescripcion(), permisos);
    }
}
