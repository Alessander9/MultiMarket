package com.multimarket.services.impl;

import com.multimarket.dto.CategoriaRequest;
import com.multimarket.dto.CategoriaResponse;
import com.multimarket.models.Categoria;
import com.multimarket.repositories.CategoriaRepository;
import com.multimarket.services.Interfaces.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        categoriaRepository.findByNombre(request.getNombre())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe una categoría registrada con ese nombre.");
                });

        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setActiva(true);

        Categoria guardada = categoriaRepository.save(categoria);
        return convertToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategoriasActivas() {
        return categoriaRepository.findByActivaTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse consultarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
        return convertToResponse(categoria);
    }

    @Override
    @Transactional
    public CategoriaResponse editarCategoria(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        // Validar nombre único si está cambiando
        categoriaRepository.findByNombre(request.getNombre())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) {
                        throw new IllegalArgumentException("Ya existe otra categoría registrada con ese nombre.");
                    }
                });

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria actualizada = categoriaRepository.save(categoria);
        return convertToResponse(actualizada);
    }

    @Override
    @Transactional
    public void desactivarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
        categoria.setActiva(false);
        categoriaRepository.save(categoria);
    }

    private CategoriaResponse convertToResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getActiva());
    }
}
