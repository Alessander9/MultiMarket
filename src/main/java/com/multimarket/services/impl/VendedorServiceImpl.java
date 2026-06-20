package com.multimarket.services.impl;

import com.multimarket.dto.VendedorRequest;
import com.multimarket.dto.VendedorResponse;
import com.multimarket.models.RolNombre;
import com.multimarket.models.Usuario;
import com.multimarket.models.Vendedor;
import com.multimarket.repositories.UsuarioRepository;
import com.multimarket.repositories.VendedorRepository;
import com.multimarket.services.Interfaces.VendedorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendedorServiceImpl implements VendedorService {

    private final VendedorRepository vendedorRepository;
    private final UsuarioRepository usuarioRepository;

    public VendedorServiceImpl(VendedorRepository vendedorRepository, UsuarioRepository usuarioRepository) {
        this.vendedorRepository = vendedorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public VendedorResponse crearTienda(String correoUsuario, VendedorRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar si el usuario tiene el rol de VENDEDOR
        boolean esVendedor = usuario.getRoles().stream()
                .anyMatch(rol -> rol.getNombre() == RolNombre.VENDEDOR);

        if (!esVendedor) {
            throw new IllegalStateException("El usuario debe tener asignado el rol de VENDEDOR para registrar una tienda.");
        }

        // Validar si ya tiene una tienda registrada
        if (vendedorRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya tiene una tienda registrada en el marketplace.");
        }

        if (vendedorRepository.findByNombreTienda(request.getNombreTienda()).isPresent()) {
            throw new IllegalArgumentException("El nombre de la tienda ya está en uso.");
        }

        Vendedor tienda = new Vendedor();
        tienda.setUsuario(usuario);
        tienda.setNombreTienda(request.getNombreTienda());
        tienda.setDescripcion(request.getDescripcion());
        tienda.setRegion(request.getRegion());
        tienda.setDireccion(request.getDireccion());
        tienda.setLogo(request.getLogo());
        tienda.setBanner(request.getBanner());
        tienda.setActivo(true);
        tienda.setFechaCreacion(LocalDateTime.now());
        tienda.setCalificacionPromedio(BigDecimal.ZERO);

        Vendedor guardada = vendedorRepository.save(tienda);
        return convertToResponse(guardada);
    }

    @Override
    @Transactional
    public VendedorResponse editarTienda(Long id, String correoUsuario, boolean esAdmin, VendedorRequest request) {
        Vendedor tienda = vendedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada"));

        // Administradores pueden editar cualquier tienda; el vendedor solo la suya
        if (!esAdmin && !tienda.getUsuario().getCorreo().equals(correoUsuario)) {
            throw new SecurityException("No estás autorizado para modificar esta tienda.");
        }

        // Validar nombre único
        vendedorRepository.findByNombreTienda(request.getNombreTienda())
                .ifPresent(v -> {
                    if (!v.getId().equals(id)) {
                        throw new IllegalArgumentException("El nombre de la tienda ya está en uso.");
                    }
                });

        tienda.setNombreTienda(request.getNombreTienda());
        tienda.setDescripcion(request.getDescripcion());
        tienda.setRegion(request.getRegion());
        tienda.setDireccion(request.getDireccion());
        tienda.setLogo(request.getLogo());
        tienda.setBanner(request.getBanner());

        Vendedor actualizada = vendedorRepository.save(tienda);
        return convertToResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorResponse consultarTienda(Long id) {
        Vendedor tienda = vendedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada"));
        return convertToResponse(tienda);
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorResponse consultarMiTienda(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Vendedor tienda = vendedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Aún no tienes una tienda registrada."));

        return convertToResponse(tienda);
    }

    @Override
    @Transactional
    public VendedorResponse desactivarTienda(Long id, String correoUsuario, boolean esAdmin, boolean activo) {
        Vendedor tienda = vendedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada"));

        // Administradores pueden cambiar cualquier tienda; el vendedor solo la suya
        if (!esAdmin && !tienda.getUsuario().getCorreo().equals(correoUsuario)) {
            throw new SecurityException("No estás autorizado para cambiar el estado de esta tienda.");
        }

        tienda.setActivo(activo);
        Vendedor actualizada = vendedorRepository.save(tienda);
        return convertToResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendedorResponse> listarTodos() {
        return vendedorRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private VendedorResponse convertToResponse(Vendedor v) {
        return new VendedorResponse(
                v.getId(),
                v.getUsuario().getId(),
                v.getNombreTienda(),
                v.getDescripcion(),
                v.getRegion(),
                v.getDireccion(),
                v.getLogo(),
                v.getBanner(),
                v.getFechaCreacion(),
                v.getActivo(),
                v.getCalificacionPromedio()
        );
    }
}
