package com.multimarket.repositories;

import com.multimarket.models.ImportacionCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportacionCatalogoRepository extends JpaRepository<ImportacionCatalogo, Long> {
}
