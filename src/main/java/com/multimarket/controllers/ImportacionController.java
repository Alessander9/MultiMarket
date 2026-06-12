package com.multimarket.controllers;

import com.multimarket.models.ImportacionCatalogo;
import com.multimarket.services.Interfaces.ImportacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.multimarket.repositories.ImportacionCatalogoRepository;
import java.util.List;

@RestController
@RequestMapping("/importar")
public class ImportacionController {

    private final ImportacionService importacionService;
    private final ImportacionCatalogoRepository importacionCatalogoRepository;

    public ImportacionController(ImportacionService importacionService,
                                 ImportacionCatalogoRepository importacionCatalogoRepository) {
        this.importacionService = importacionService;
        this.importacionCatalogoRepository = importacionCatalogoRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ImportacionCatalogo> subirXml(@RequestParam("file") MultipartFile file) {
        ImportacionCatalogo result = importacionService.importarCatalogoXml(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<ImportacionCatalogo>> listarImportaciones() {
        return ResponseEntity.ok(importacionCatalogoRepository.findAll());
    }
}
