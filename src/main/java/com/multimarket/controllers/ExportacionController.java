package com.multimarket.controllers;

import com.multimarket.models.ExportacionCatalogo;
import com.multimarket.models.FormatoExportacion;
import com.multimarket.services.Interfaces.ExportacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.multimarket.repositories.ExportacionCatalogoRepository;
import java.util.List;

@RestController
@RequestMapping("/exportar")
public class ExportacionController {

    private final ExportacionService exportacionService;
    private final ExportacionCatalogoRepository exportacionCatalogoRepository;

    public ExportacionController(ExportacionService exportacionService,
                                 ExportacionCatalogoRepository exportacionCatalogoRepository) {
        this.exportacionService = exportacionService;
        this.exportacionCatalogoRepository = exportacionCatalogoRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ExportacionCatalogo> exportar(@RequestParam("formato") FormatoExportacion formato) {
        ExportacionCatalogo result = exportacionService.programarExportacion(formato);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<ExportacionCatalogo>> listarExportaciones() {
        return ResponseEntity.ok(exportacionCatalogoRepository.findAll());
    }
}
