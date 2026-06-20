package com.multimarket.clients;

import com.multimarket.dto.CategoriaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "catalogIntegrationClient",
        url = "${multimarket.integrations.catalog-url:http://localhost:8080}"
)
public interface CatalogIntegrationClient {

    @GetMapping("/categorias")
    List<CategoriaResponse> listarCategorias();
}
