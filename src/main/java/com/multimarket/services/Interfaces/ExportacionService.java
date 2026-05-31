package com.multimarket.services.Interfaces;

import com.multimarket.models.ExportacionCatalogo;
import com.multimarket.models.FormatoExportacion;

public interface ExportacionService {
    ExportacionCatalogo programarExportacion(FormatoExportacion formato);
}
