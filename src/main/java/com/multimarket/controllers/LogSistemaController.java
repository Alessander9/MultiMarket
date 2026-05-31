package com.multimarket.controllers;

import com.multimarket.models.LogSistema;
import com.multimarket.models.NivelLog;
import com.multimarket.models.ModuloSistema;
import com.multimarket.services.Interfaces.LogSistemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogSistemaController {

    private final LogSistemaService logService;

    public LogSistemaController(LogSistemaService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<List<LogSistema>> getLogs() {
        List<LogSistema> result = logService.consultarLogs();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<LogSistema>> getLogsFiltrados(@RequestParam(value = "nivel", required = false) NivelLog nivel,
                                                             @RequestParam(value = "modulo", required = false) ModuloSistema modulo) {
        List<LogSistema> result = logService.filtrarLogs(nivel, modulo);
        return ResponseEntity.ok(result);
    }
}
