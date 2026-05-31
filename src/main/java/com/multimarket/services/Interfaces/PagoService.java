package com.multimarket.services.Interfaces;

import com.multimarket.dto.PagoRequest;
import com.multimarket.dto.PagoResponse;
import java.math.BigDecimal;

public interface PagoService {
    // REST API Entrypoint
    PagoResponse procesarPago(String compradorCorreo, PagoRequest request);
    PagoResponse consultarPago(Long id, String usuarioCorreo);

    // SOAP Simulated Web Service Operations (JAX-WS emulation)
    boolean validarTarjeta(String numeroTarjeta, String cvv, String fechaExpiracion);
    String procesarPagoSOAP(String numeroTarjeta, BigDecimal monto);
    String consultarOperacionSOAP(String codigoOperacion);
}
