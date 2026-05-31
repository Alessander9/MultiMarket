package com.multimarket.services.Interfaces;

import com.multimarket.dto.CategoriaRequest;
import com.multimarket.dto.CategoriaResponse;
import java.util.List;

public interface CategoriaService {
    CategoriaResponse crearCategoria(CategoriaRequest request);
    List<CategoriaResponse> listarCategoriasActivas();
    CategoriaResponse consultarCategoria(Long id);
    CategoriaResponse editarCategoria(Long id, CategoriaRequest request);
    void desactivarCategoria(Long id);
}
