package com.multimarket.services.impl;

import com.multimarket.dto.ImagenProductoResponse;
import com.multimarket.dto.ProductoRequest;
import com.multimarket.dto.ProductoResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.services.Interfaces.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VendedorRepository vendedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ImagenProductoRepository imagenRepository;
    private final ProductoFavoritoRepository favoritoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                               VendedorRepository vendedorRepository, UsuarioRepository usuarioRepository,
                               ImagenProductoRepository imagenRepository, ProductoFavoritoRepository favoritoRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.vendedorRepository = vendedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.imagenRepository = imagenRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Override
    @Transactional
    public ProductoResponse crearProducto(String correoUsuario, ProductoRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);

        Vendedor tienda;
        if (esAdmin) {
            if (request.getVendedorId() == null) {
                throw new IllegalArgumentException("El ID del vendedor es obligatorio para el administrador.");
            }
            tienda = vendedorRepository.findById(request.getVendedorId())
                    .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));
        } else {
            tienda = vendedorRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new IllegalStateException("Debes registrar una tienda antes de publicar productos."));
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (!categoria.getActiva()) {
            throw new IllegalArgumentException("La categoría seleccionada no se encuentra activa");
        }

        productoRepository.findBySku(request.getSku())
                .ifPresent(p -> {
                    throw new IllegalArgumentException("El código SKU ya está en uso por otro producto.");
                });

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setSku(request.getSku());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setPeso(request.getPeso());
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());
        producto.setCategoria(categoria);
        producto.setVendedor(tienda);

        Producto guardado = productoRepository.save(producto);
        return convertToResponse(guardado);
    }

    @Override
    @Transactional
    public ProductoResponse editarProducto(Long id, String correoUsuario, ProductoRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);

        // Validar propietario
        if (!esAdmin && !producto.getVendedor().getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No estás autorizado para modificar este producto.");
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (!categoria.getActiva()) {
            throw new IllegalArgumentException("La categoría seleccionada no se encuentra activa");
        }

        // Validar SKU
        productoRepository.findBySku(request.getSku())
                .ifPresent(p -> {
                    if (!p.getId().equals(id)) {
                        throw new IllegalArgumentException("El código SKU ya está en uso por otro producto.");
                    }
                });

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setSku(request.getSku());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setPeso(request.getPeso());
        producto.setCategoria(categoria);
        producto.setFechaActualizacion(LocalDateTime.now());

        Producto actualizado = productoRepository.save(producto);
        return convertToResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse consultarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        return convertToResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosActivos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarMisProductos(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Vendedor tienda = vendedorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Aún no tienes una tienda registrada."));

        return productoRepository.findByVendedorUsuarioIdOrderByIdDesc(tienda.getUsuario().getId()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosPorTienda(Long vendedorId) {
        return productoRepository.findByVendedorIdOrderByIdDesc(vendedorId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void desactivarProducto(Long id, String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);

        if (!esAdmin && !producto.getVendedor().getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No estás autorizado para desactivar este producto.");
        }

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductos(String nombre, Long categoriaId, Long vendedorId,
                                                 BigDecimal minPrecio, BigDecimal maxPrecio) {
        return productoRepository.searchProducts(nombre, categoriaId, vendedorId, minPrecio, maxPrecio).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ImagenProductoResponse agregarImagen(Long productoId, String correoUsuario, String url, boolean principal, int orden) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);

        if (!esAdmin && !producto.getVendedor().getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No estás autorizado para modificar las imágenes de este producto.");
        }

        // Si se marca como principal, desactivar las principales previas
        if (principal) {
            for (ImagenProducto img : producto.getImagenes()) {
                if (img.getPrincipal()) {
                    img.setPrincipal(false);
                    imagenRepository.save(img);
                }
            }
        }

        ImagenProducto nuevaImg = new ImagenProducto();
        nuevaImg.setUrl(url);
        nuevaImg.setPrincipal(principal);
        nuevaImg.setOrdenVisualizacion(orden);
        nuevaImg.setProducto(producto);

        ImagenProducto guardada = imagenRepository.save(nuevaImg);
        return new ImagenProductoResponse(guardada.getId(), guardada.getUrl(), guardada.getPrincipal(), guardada.getOrdenVisualizacion());
    }

    @Override
    @Transactional
    public void eliminarImagen(Long imagenId, String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        ImagenProducto imagen = imagenRepository.findById(imagenId)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));

        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);

        if (!esAdmin && !imagen.getProducto().getVendedor().getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No estás autorizado para eliminar imágenes de este producto.");
        }

        imagenRepository.delete(imagen);
    }

    @Override
    @Transactional
    public String agregarFavorito(String correoUsuario, Long productoId) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (favoritoRepository.existsByUsuarioIdAndProductoId(usuario.getId(), producto.getId())) {
            return "El producto ya está en tu lista de favoritos";
        }

        ProductoFavorito fav = new ProductoFavorito();
        fav.setUsuario(usuario);
        fav.setProducto(producto);
        fav.setFechaAgregado(LocalDateTime.now());

        favoritoRepository.save(fav);
        return "Producto agregado a favoritos";
    }

    @Override
    @Transactional
    public String eliminarFavorito(String correoUsuario, Long productoId) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        ProductoFavorito fav = favoritoRepository.findByUsuarioIdAndProductoId(usuario.getId(), productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto no está en tu lista de favoritos"));

        favoritoRepository.delete(fav);
        return "Producto eliminado de favoritos";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarFavoritos(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return favoritoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(fav -> convertToResponse(fav.getProducto()))
                .collect(Collectors.toList());
    }

    private ProductoResponse convertToResponse(Producto p) {
        List<ImagenProductoResponse> imagenesResponse = p.getImagenes().stream()
                .map(img -> new ImagenProductoResponse(img.getId(), img.getUrl(), img.getPrincipal(), img.getOrdenVisualizacion()))
                .collect(Collectors.toList());

        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getSku(),
                p.getPrecio(),
                p.getStock(),
                p.getPeso(),
                p.getActivo(),
                p.getFechaCreacion(),
                p.getFechaActualizacion(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getVendedor().getId(),
                p.getVendedor().getNombreTienda(),
                imagenesResponse
        );
    }
}
