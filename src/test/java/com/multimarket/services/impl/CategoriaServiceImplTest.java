package com.multimarket.services.impl;

import com.multimarket.dto.CategoriaRequest;
import com.multimarket.models.Categoria;
import com.multimarket.repositories.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl service;

    @Test
    void crearCategoriaShouldRejectDuplicateName() {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("Cafe");
        request.setDescripcion("Demo");

        when(categoriaRepository.findByNombre("Cafe")).thenReturn(Optional.of(new Categoria()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crearCategoria(request));
        assertEquals("Ya existe una categoría registrada con ese nombre.", ex.getMessage());
    }

    @Test
    void editarCategoriaShouldUpdateFields() {
        Categoria existing = new Categoria();
        existing.setId(3L);
        existing.setNombre("Cafe");
        existing.setDescripcion("Old");
        existing.setActiva(true);

        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("Cafe Premium");
        request.setDescripcion("Nuevo");

        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(categoriaRepository.findByNombre("Cafe Premium")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.editarCategoria(3L, request);

        assertEquals("Cafe Premium", response.getNombre());
        assertEquals("Nuevo", response.getDescripcion());
    }
}
