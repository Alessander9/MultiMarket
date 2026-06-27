package com.multimarket.controllers;

import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.services.Interfaces.CompraAgrupadaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
public class CompraAgrupadaController {

    private final CompraAgrupadaService compraAgrupadaService;

    public CompraAgrupadaController(CompraAgrupadaService compraAgrupadaService) {
        this.compraAgrupadaService = compraAgrupadaService;
    }

    @GetMapping("/mis-compras")
    public ResponseEntity<List<CompraAgrupadaResponse>> listarMisCompras(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(compraAgrupadaService.listarComprasPorComprador(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraAgrupadaResponse> consultarCompra(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(compraAgrupadaService.consultarCompra(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarBoletaPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] pdf = compraAgrupadaService.generarBoletaPdf(id, userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=boleta-compra-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
