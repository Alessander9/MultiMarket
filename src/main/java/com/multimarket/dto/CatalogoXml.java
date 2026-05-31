package com.multimarket.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "catalogo")
@XmlAccessorType(XmlAccessType.FIELD)
public class CatalogoXml {

    @XmlElement(name = "producto")
    private List<ProductoXml> productos = new ArrayList<>();

    public CatalogoXml() {}

    public List<ProductoXml> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoXml> productos) {
        this.productos = productos;
    }
}
