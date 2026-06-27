package com.multimarket.controllers;

import com.multimarket.dto.CompraAgrupadaPagoRequest;
import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.dto.PagoRequest;
import com.multimarket.dto.PagoResponse;
import com.multimarket.dto.SoapTransactionResponse;
import com.multimarket.services.Interfaces.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> procesarPago(
            @Valid @RequestBody PagoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PagoResponse response = pagoService.procesarPago(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compra-agrupada")
    public ResponseEntity<CompraAgrupadaResponse> procesarCompraAgrupada(
            @Valid @RequestBody CompraAgrupadaPagoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CompraAgrupadaResponse response = pagoService.procesarCompraAgrupada(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> consultarPago(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PagoResponse response = pagoService.consultarPago(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagoResponse>> listarPagos() {
        return ResponseEntity.ok(pagoService.listarPagos());
    }

    @GetMapping("/mi-historial")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<List<PagoResponse>> listarPagosPorVendedor(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pagoService.listarPagosPorVendedor(userDetails.getUsername()));
    }

    @GetMapping("/soap")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SoapTransactionResponse>> listarTransaccionesSOAP() {
        return ResponseEntity.ok(pagoService.listarTransaccionesSOAP());
    }
}
