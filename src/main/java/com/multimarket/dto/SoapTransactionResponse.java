package com.multimarket.dto;

import java.time.LocalDateTime;

public class SoapTransactionResponse {

    private Long id;
    private String requestXml;
    private String responseXml;
    private LocalDateTime fecha;
    private String estado;

    public SoapTransactionResponse() {}

    public SoapTransactionResponse(Long id, String requestXml, String responseXml, LocalDateTime fecha, String estado) {
        this.id = id;
        this.requestXml = requestXml;
        this.responseXml = responseXml;
        this.fecha = fecha;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestXml() { return requestXml; }
    public void setRequestXml(String requestXml) { this.requestXml = requestXml; }
    public String getResponseXml() { return responseXml; }
    public void setResponseXml(String responseXml) { this.responseXml = responseXml; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
