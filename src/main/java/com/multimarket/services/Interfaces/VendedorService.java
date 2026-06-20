package com.multimarket.services.Interfaces;

import com.multimarket.dto.VendedorRequest;
import com.multimarket.dto.VendedorResponse;
import java.util.List;

public interface VendedorService {
    VendedorResponse crearTienda(String correoUsuario, VendedorRequest request);
    VendedorResponse editarTienda(Long id, String correoUsuario, boolean esAdmin, VendedorRequest request);
    VendedorResponse consultarTienda(Long id);
    VendedorResponse consultarMiTienda(String correoUsuario);
    VendedorResponse desactivarTienda(Long id, String correoUsuario, boolean esAdmin, boolean activo);
    List<VendedorResponse> listarTodos();
}
