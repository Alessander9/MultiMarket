package com.multimarket.services.Interfaces;

import com.multimarket.dto.CompraAgrupadaResponse;

import java.util.List;

public interface CompraAgrupadaService {
    List<CompraAgrupadaResponse> listarComprasPorComprador(String compradorCorreo);
    CompraAgrupadaResponse consultarCompra(Long id, String compradorCorreo);
    byte[] generarBoletaPdf(Long id, String compradorCorreo);
}
