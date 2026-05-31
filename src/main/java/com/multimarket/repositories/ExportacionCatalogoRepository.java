package com.multimarket.repositories;

import com.multimarket.models.ExportacionCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportacionCatalogoRepository extends JpaRepository<ExportacionCatalogo, Long> {
}
