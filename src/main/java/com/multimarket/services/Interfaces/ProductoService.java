package com.multimarket.services.Interfaces;

import com.multimarket.dto.ImagenProductoResponse;
import com.multimarket.dto.ProductoRequest;
import com.multimarket.dto.ProductoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoService {
    ProductoResponse crearProducto(String correoUsuario, ProductoRequest request);
    ProductoResponse editarProducto(Long id, String correoUsuario, ProductoRequest request);
    ProductoResponse consultarProducto(Long id);
    List<ProductoResponse> listarProductosActivos();
    void desactivarProducto(Long id, String correoUsuario);
    
    // Búsqueda avanzada
    List<ProductoResponse> buscarProductos(String nombre, Long categoriaId, Long vendedorId,
                                           BigDecimal minPrecio, BigDecimal maxPrecio);

    // CRUD Imágenes
    ImagenProductoResponse agregarImagen(Long productoId, String correoUsuario, String url, boolean principal, int orden);
    void eliminarImagen(Long imagenId, String correoUsuario);

    // Favoritos
    String agregarFavorito(String correoUsuario, Long productoId);
    String eliminarFavorito(String correoUsuario, Long productoId);
    List<ProductoResponse> listarFavoritos(String correoUsuario);
}
