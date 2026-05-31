package com.multimarket.services.impl;

import com.multimarket.models.LogSistema;
import com.multimarket.models.NivelLog;
import com.multimarket.models.ModuloSistema;
import com.multimarket.repositories.LogSistemaRepository;
import com.multimarket.services.Interfaces.LogSistemaService;
import com.multimarket.kafka.KafkaProducer;
import com.multimarket.kafka.events.LogGeneradoEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Service
@Transactional
public class LogSistemaServiceImpl implements LogSistemaService {

    private final LogSistemaRepository logRepository;
    private final KafkaProducer kafkaProducer;

    public LogSistemaServiceImpl(LogSistemaRepository logRepository,
                                  KafkaProducer kafkaProducer) {
        this.logRepository = logRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void registrarLog(NivelLog nivel, ModuloSistema modulo, String accion, String descripcion,
                             String ipOrigen, String endpoint, String metodoHttp, Long usuarioId,
                             String requestData, String responseData, String stackTrace, Boolean exitoso) {
        // Enviar evento de log de forma asíncrona mediante Kafka
        kafkaProducer.sendLogEvent(new LogGeneradoEvent(
                nivel.name(),
                modulo.name(),
                accion,
                descripcion,
                ipOrigen,
                endpoint,
                metodoHttp,
                usuarioId,
                requestData,
                responseData,
                stackTrace,
                exitoso
        ));
    }

    @Override
    public void registrarError(ModuloSistema modulo, String accion, String descripcion, String ipOrigen,
                               String endpoint, String metodoHttp, Long usuarioId, String requestData, String stackTrace) {
        registrarLog(NivelLog.ERROR, modulo, accion, descripcion, ipOrigen, endpoint, metodoHttp, usuarioId, requestData, "", stackTrace, false);
    }

    @Override
    public void registrarExcepcion(ModuloSistema modulo, String accion, Exception ex, String ipOrigen,
                                   String endpoint, String metodoHttp, Long usuarioId) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();

        registrarLog(
                NivelLog.ERROR,
                modulo,
                accion,
                "Excepción capturada: " + ex.getMessage(),
                ipOrigen,
                endpoint,
                metodoHttp,
                usuarioId,
                "",
                "",
                stackTrace,
                false
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogSistema> consultarLogs() {
        return logRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogSistema> filtrarLogs(NivelLog nivel, ModuloSistema modulo) {
        if (nivel != null && modulo != null) {
            return logRepository.findByNivelAndModuloOrderByFechaHoraDesc(nivel, modulo);
        } else if (nivel != null) {
            return logRepository.findByNivelOrderByFechaHoraDesc(nivel);
        } else if (modulo != null) {
            return logRepository.findByModuloOrderByFechaHoraDesc(modulo);
        } else {
            return logRepository.findAll();
        }
    }
}
