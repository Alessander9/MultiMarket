package com.multimarket.kafka.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LogGeneradoEvent implements Serializable {
    private String nivel;
    private String modulo;
    private String accion;
    private String descripcion;
    private String ipOrigen;
    private String endpoint;
    private String metodoHttp;
    private Long usuarioId;
    private String requestData;
    private String responseData;
    private String stackTrace;
    private Boolean exitoso;
    private LocalDateTime fechaHora;

    public LogGeneradoEvent() {}

    public LogGeneradoEvent(String nivel, String modulo, String accion, String descripcion, String ipOrigen,
                              String endpoint, String metodoHttp, Long usuarioId, String requestData,
                              String responseData, String stackTrace, Boolean exitoso) {
        this.nivel = nivel;
        this.modulo = modulo;
        this.accion = accion;
        this.descripcion = descripcion;
        this.ipOrigen = ipOrigen;
        this.endpoint = endpoint;
        this.metodoHttp = metodoHttp;
        this.usuarioId = usuarioId;
        this.requestData = requestData;
        this.responseData = responseData;
        this.stackTrace = stackTrace;
        this.exitoso = exitoso;
        this.fechaHora = LocalDateTime.now();
    }

    // Getters y Setters
    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public void setMetodoHttp(String metodoHttp) {
        this.metodoHttp = metodoHttp;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Boolean getExitoso() {
        return exitoso;
    }

    public void setExitoso(Boolean exitoso) {
        this.exitoso = exitoso;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
