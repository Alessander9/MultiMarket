package com.multimarket.services.Interfaces;

import com.multimarket.dto.VendedorRequest;
import com.multimarket.dto.VendedorResponse;

public interface VendedorService {
    VendedorResponse crearTienda(String correoUsuario, VendedorRequest request);
    VendedorResponse editarTienda(Long id, String correoUsuario, VendedorRequest request);
    VendedorResponse consultarTienda(Long id);
    VendedorResponse consultarMiTienda(String correoUsuario);
    VendedorResponse desactivarTienda(Long id, String correoUsuario, boolean activo);
}
