package com.multimarket.repositories;

import com.multimarket.models.LogSistema;
import com.multimarket.models.NivelLog;
import com.multimarket.models.ModuloSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogSistemaRepository extends JpaRepository<LogSistema, Long> {
    List<LogSistema> findByNivelOrderByFechaHoraDesc(NivelLog nivel);
    List<LogSistema> findByModuloOrderByFechaHoraDesc(ModuloSistema modulo);
    List<LogSistema> findByNivelAndModuloOrderByFechaHoraDesc(NivelLog nivel, ModuloSistema modulo);
}
