package com.multimarket.controllers;

import com.multimarket.dto.CategoriaRequest;
import com.multimarket.dto.CategoriaResponse;
import com.multimarket.services.Interfaces.CategoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @Test
    void createCategoryShouldReturnCreatedPayload() {
        CategoriaController controller = new CategoriaController(categoriaService);
        CategoriaResponse response = new CategoriaResponse(11L, "Cafe", "Especialidad", true);
        when(categoriaService.crearCategoria(any())).thenReturn(response);

        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("Cafe");
        request.setDescripcion("Especialidad");

        var result = controller.crearCategoria(request);

        assertEquals("Cafe", result.getBody().getNombre());
        assertEquals(true, result.getBody().getActiva());
    }

    @Test
    void listCategoriesShouldReturnActives() {
        CategoriaController controller = new CategoriaController(categoriaService);
        when(categoriaService.listarCategoriasActivas()).thenReturn(List.of(new CategoriaResponse(1L, "Cafe", "Demo", true)));

        var result = controller.listarCategoriasActivas();

        assertEquals(1, result.getBody().size());
        assertEquals("Cafe", result.getBody().get(0).getNombre());
    }
}
