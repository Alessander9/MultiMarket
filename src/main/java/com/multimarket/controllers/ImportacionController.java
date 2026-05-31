package com.multimarket.controllers;

import com.multimarket.models.ImportacionCatalogo;
import com.multimarket.services.Interfaces.ImportacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/importar")
public class ImportacionController {

    private final ImportacionService importacionService;

    public ImportacionController(ImportacionService importacionService) {
        this.importacionService = importacionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ImportacionCatalogo> subirXml(@RequestParam("file") MultipartFile file) {
        ImportacionCatalogo result = importacionService.importarCatalogoXml(file);
        return ResponseEntity.ok(result);
    }
}
