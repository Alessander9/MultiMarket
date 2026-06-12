package com.multimarket.controllers;

import com.multimarket.dto.AdminUserResponse;
import com.multimarket.models.Usuario;
import com.multimarket.services.Interfaces.AuthService;
import com.multimarket.repositories.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AdminUserResponse>> listarUsuarios() {
        List<AdminUserResponse> response = usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .map(usuario -> new AdminUserResponse(
                        usuario.getId(),
                        usuario.getCorreo(),
                        usuario.getRoles().stream().map(rol -> rol.getNombre().name()).collect(Collectors.toList()),
                        usuario.getEstado(),
                        usuario.getCorreoVerificado(),
                        usuario.getFechaRegistro(),
                        usuario.getIntentosFallidos(),
                        usuario.getBloqueado(),
                        usuario.getPerfil() != null ? usuario.getPerfil().getNombres() : null,
                        usuario.getPerfil() != null ? usuario.getPerfil().getApellidos() : null,
                        usuario.getPerfil() != null ? usuario.getPerfil().getDni() : null,
                        usuario.getPerfil() != null ? usuario.getPerfil().getTelefono() : null,
                        usuario.getPerfil() != null ? usuario.getPerfil().getDireccion() : null,
                        usuario.getPerfil() != null ? usuario.getPerfil().getFotoPerfil() : null,
                        usuario.getPerfil() != null ? usuario.getPerfil().getFechaNacimiento() : null
                ))
                .toList();
        return ResponseEntity.ok(response);
    }
}
