package com.multimarket.services.Interfaces;

import com.multimarket.models.ImportacionCatalogo;
import org.springframework.web.multipart.MultipartFile;

public interface ImportacionService {
    ImportacionCatalogo importarCatalogoXml(MultipartFile file);
}
