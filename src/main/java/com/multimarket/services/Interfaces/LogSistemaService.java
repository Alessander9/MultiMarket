package com.multimarket.services.Interfaces;

import com.multimarket.models.LogSistema;
import com.multimarket.models.NivelLog;
import com.multimarket.models.ModuloSistema;
import java.util.List;

public interface LogSistemaService {
    void registrarLog(NivelLog nivel, ModuloSistema modulo, String accion, String descripcion, String ipOrigen, String endpoint, String metodoHttp, Long usuarioId, String requestData, String responseData, String stackTrace, Boolean exitoso);
    void registrarError(ModuloSistema modulo, String accion, String descripcion, String ipOrigen, String endpoint, String metodoHttp, Long usuarioId, String requestData, String stackTrace);
    void registrarExcepcion(ModuloSistema modulo, String accion, Exception ex, String ipOrigen, String endpoint, String metodoHttp, Long usuarioId);
    List<LogSistema> consultarLogs();
    List<LogSistema> filtrarLogs(NivelLog nivel, ModuloSistema modulo);
}
