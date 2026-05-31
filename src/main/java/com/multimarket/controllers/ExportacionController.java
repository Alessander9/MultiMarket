package com.multimarket.controllers;

import com.multimarket.models.ExportacionCatalogo;
import com.multimarket.models.FormatoExportacion;
import com.multimarket.services.Interfaces.ExportacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exportar")
public class ExportacionController {

    private final ExportacionService exportacionService;

    public ExportacionController(ExportacionService exportacionService) {
        this.exportacionService = exportacionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ExportacionCatalogo> exportar(@RequestParam("formato") FormatoExportacion formato) {
        ExportacionCatalogo result = exportacionService.programarExportacion(formato);
        return ResponseEntity.ok(result);
    }
}
