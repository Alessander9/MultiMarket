package com.multimarket.dto;

public class ImagenProductoResponse {

    private Long id;
    private String url;
    private Boolean principal;
    private Integer ordenVisualizacion;

    public ImagenProductoResponse() {}

    public ImagenProductoResponse(Long id, String url, Boolean principal, Integer ordenVisualizacion) {
        this.id = id;
        this.url = url;
        this.principal = principal;
        this.ordenVisualizacion = ordenVisualizacion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public Integer getOrdenVisualizacion() {
        return ordenVisualizacion;
    }

    public void setOrdenVisualizacion(Integer ordenVisualizacion) {
        this.ordenVisualizacion = ordenVisualizacion;
    }
}
