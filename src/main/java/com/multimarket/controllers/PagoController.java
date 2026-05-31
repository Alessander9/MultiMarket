package com.multimarket.controllers;

import com.multimarket.dto.PagoRequest;
import com.multimarket.dto.PagoResponse;
import com.multimarket.services.Interfaces.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> consultarPago(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PagoResponse response = pagoService.consultarPago(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
