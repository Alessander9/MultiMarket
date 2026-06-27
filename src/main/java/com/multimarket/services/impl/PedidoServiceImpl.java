package com.multimarket.services.impl;

import com.multimarket.dto.*;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.services.Interfaces.PedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             DetallePedidoRepository detallePedidoRepository,
                             UsuarioRepository usuarioRepository,
                             VendedorRepository vendedorRepository,
                             ProductoRepository productoRepository,
                             InventarioRepository inventarioRepository,
                             MovimientoInventarioRepository movimientoInventarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.vendedorRepository = vendedorRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Override
    public PedidoResponse crearPedido(String compradorCorreo, PedidoRequest request) {
        // 1. Validar Comprador
        Usuario comprador = usuarioRepository.findByCorreo(compradorCorreo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el comprador con el correo: " + compradorCorreo));

        // 2. Validar Vendedor
        Vendedor vendedor = vendedorRepository.findById(request.getVendedorId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el vendedor con el ID: " + request.getVendedorId()));

        // Restricción: No se puede comprar a uno mismo (si el comprador es el dueño de la tienda)
        if (vendedor.getUsuario().getId().equals(comprador.getId())) {
            throw new IllegalArgumentException("Restricción de Negocio: No puedes realizar compras en tu propia tienda.");
        }

        // 3. Generar número de pedido único: MM-YYYYMMDD-XXXX
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randStr = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String numeroPedido = "MM-" + dateStr + "-" + randStr;

        // 4. Crear el pedido base
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(numeroPedido);
        pedido.setComprador(comprador);
        pedido.setVendedor(vendedor);
        pedido.setCostoEnvio(request.getCostoEnvio());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal acumuladoSubtotal = BigDecimal.ZERO;

        // 5. Procesar líneas de detalle y validar stock
        for (DetallePedidoRequest detReq : request.getDetalles()) {
            Producto producto = productoRepository.findById(detReq.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con el ID: " + detReq.getProductoId()));

            // Validar que el producto realmente pertenezca a la tienda del vendedor especificado
            if (!producto.getVendedor().getId().equals(vendedor.getId())) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no pertenece a la tienda seleccionada.");
            }

            if (!producto.getActivo()) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no está activo para la venta.");
            }

            // Validar e inicializar Inventario si es necesario (self-healing)
            Inventario inventario = inventarioRepository.findByProductoId(producto.getId())
                    .orElseGet(() -> {
                        Inventario nuevo = new Inventario();
                        nuevo.setStockActual(producto.getStock());
                        nuevo.setStockMinimo(0);
                        nuevo.setProducto(producto);
                        nuevo.setUltimaActualizacion(LocalDateTime.now());
                        return inventarioRepository.save(nuevo);
                    });

            // Validar Stock
            if (inventario.getStockActual() < detReq.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto '" + producto.getNombre() + "'. Stock disponible: " + inventario.getStockActual() + ", Solicitado: " + detReq.getCantidad());
            }

            // Descontar inventario
            int nuevoStock = inventario.getStockActual() - detReq.getCantidad();
            inventario.setStockActual(nuevoStock);
            inventario.setUltimaActualizacion(LocalDateTime.now());
            inventarioRepository.save(inventario);

            // Sincronizar campo stock en la tabla Producto
            producto.setStock(nuevoStock);
            productoRepository.save(producto);

            // Registrar movimiento de salida
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
            movimiento.setCantidad(detReq.getCantidad());
            movimiento.setObservacion("Despacho por creación de pedido: " + numeroPedido);
            movimiento.setInventario(inventario);
            movimientoInventarioRepository.save(movimiento);

            // Crear Detalle
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(detReq.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            
            BigDecimal lineaSubtotal = producto.getPrecio().multiply(BigDecimal.valueOf(detReq.getCantidad()));
            detalle.setSubtotal(lineaSubtotal);
            
            acumuladoSubtotal = acumuladoSubtotal.add(lineaSubtotal);

            // Añadir relación bidireccional
            pedido.addDetalle(detalle);
        }

        // 6. Calcular montos
        pedido.setSubtotal(acumuladoSubtotal);
        // Impuesto del 18% (IGV)
        BigDecimal impuesto = acumuladoSubtotal.multiply(BigDecimal.valueOf(0.18));
        pedido.setImpuesto(impuesto);
        
        BigDecimal total = acumuladoSubtotal.add(impuesto).add(request.getCostoEnvio());
        pedido.setTotal(total);

        // 7. Guardar pedido en base de datos (guardará en cascada los detalles)
        Pedido guardado = pedidoRepository.save(pedido);

        return mapToResponse(guardado);
    }

    @Override
    public CompraAgrupadaResponse crearPedidosAgrupados(String compradorCorreo, CompraAgrupadaRequest request) {
        Usuario comprador = usuarioRepository.findByCorreo(compradorCorreo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el comprador con el correo: " + compradorCorreo));

        if (request.getGrupos() == null || request.getGrupos().isEmpty()) {
            throw new IllegalArgumentException("La compra debe contener al menos un grupo de vendedor.");
        }

        List<PedidoResponse> pedidos = request.getGrupos().stream()
                .map(grupo -> crearPedidoParaGrupo(comprador, grupo))
                .collect(Collectors.toList());

        BigDecimal subtotal = pedidos.stream()
                .map(PedidoResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal impuesto = pedidos.stream()
                .map(PedidoResponse::getImpuesto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal costoEnvioTotal = pedidos.stream()
                .map(PedidoResponse::getCostoEnvio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = pedidos.stream()
                .map(PedidoResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CompraAgrupadaResponse(null, null, null, null, "PENDIENTE", pedidos, subtotal, impuesto, costoEnvioTotal, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse consultarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el pedido con el ID: " + id));
        return mapToResponse(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorComprador(String compradorCorreo) {
        return pedidoRepository.findByCompradorCorreoOrderByFechaPedidoDesc(compradorCorreo)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorVendedor(String vendedorCorreo) {
        Vendedor vendedor = vendedorRepository.findByUsuarioCorreo(vendedorCorreo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una tienda de vendedor asociada a la cuenta: " + vendedorCorreo));

        return pedidoRepository.findByVendedorIdOrderByFechaPedidoDesc(vendedor.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PedidoResponse cancelarPedido(Long id, String usuarioCorreo) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el pedido con el ID: " + id));

        Usuario usuario = usuarioRepository.findByCorreo(usuarioCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioCorreo));

        boolean esComprador = pedido.getComprador().getId().equals(usuario.getId());
        boolean esVendedor = pedido.getVendedor().getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);

        if (!esComprador && !esVendedor && !esAdmin) {
            throw new SecurityException("No tienes permisos para cancelar este pedido.");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalArgumentException("Solo se pueden cancelar pedidos en estado PENDIENTE. Estado actual: " + pedido.getEstado());
        }

        // Ejecutar proceso de cancelación y restauración de stock
        ejecutarCancelacion(pedido);

        Pedido guardado = pedidoRepository.save(pedido);
        return mapToResponse(guardado);
    }

    @Override
    public PedidoResponse actualizarEstado(Long id, String vendedorCorreo, ActualizarEstadoPedidoRequest request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el pedido con el ID: " + id));

        // Validar que el vendedor logueado sea el dueño de la tienda que procesa el pedido
        if (!pedido.getVendedor().getUsuario().getCorreo().equals(vendedorCorreo)) {
            throw new SecurityException("No tienes permisos para actualizar pedidos de otra tienda.");
        }

        EstadoPedido nuevoEstado = request.getEstado();
        EstadoPedido estadoAnterior = pedido.getEstado();

        if (estadoAnterior == EstadoPedido.CANCELADO || estadoAnterior == EstadoPedido.ENTREGADO) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un pedido que ya está finalizado (" + estadoAnterior + ").");
        }

        if (nuevoEstado == EstadoPedido.CANCELADO) {
            ejecutarCancelacion(pedido);
        } else {
            pedido.setEstado(nuevoEstado);
        }

        Pedido guardado = pedidoRepository.save(pedido);
        return mapToResponse(guardado);
    }

    // Método centralizado para restaurar el stock al cancelar
    private void ejecutarCancelacion(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);

        // Restaurar inventarios
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            
            Inventario inventario = inventarioRepository.findByProductoId(producto.getId())
                    .orElseGet(() -> {
                        Inventario nuevo = new Inventario();
                        nuevo.setStockActual(producto.getStock());
                        nuevo.setStockMinimo(0);
                        nuevo.setProducto(producto);
                        nuevo.setUltimaActualizacion(LocalDateTime.now());
                        return inventarioRepository.save(nuevo);
                    });

            // Sumar la cantidad reservada nuevamente al stock
            int stockRestaurado = inventario.getStockActual() + detalle.getCantidad();
            inventario.setStockActual(stockRestaurado);
            inventario.setUltimaActualizacion(LocalDateTime.now());
            inventarioRepository.save(inventario);

            // Sincronizar Producto
            producto.setStock(stockRestaurado);
            productoRepository.save(producto);

            // Registrar movimiento de entrada por devolución
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setObservacion("Devolución de stock por cancelación del pedido: " + pedido.getNumeroPedido());
            movimiento.setInventario(inventario);
            movimientoInventarioRepository.save(movimiento);
        }
    }

    private PedidoResponse mapToResponse(Pedido ped) {
        List<DetallePedidoResponse> detallesDto = ped.getDetalles().stream()
                .map(d -> new DetallePedidoResponse(
                        d.getId(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal(),
                        d.getProducto().getId(),
                        d.getProducto().getNombre()
                ))
                .collect(Collectors.toList());

        return new PedidoResponse(
                ped.getId(),
                ped.getNumeroPedido(),
                ped.getFechaPedido(),
                ped.getSubtotal(),
                ped.getImpuesto(),
                ped.getCostoEnvio(),
                ped.getTotal(),
                ped.getEstado().name(),
                ped.getComprador().getCorreo(),
                ped.getVendedor().getId(),
                ped.getVendedor().getNombreTienda(),
                detallesDto
        );
    }

    private PedidoResponse crearPedidoParaGrupo(Usuario comprador, GrupoPedidoRequest grupo) {
        if (grupo.getVendedorId() == null) {
            throw new IllegalArgumentException("Cada grupo debe indicar un vendedor.");
        }

        Vendedor vendedor = vendedorRepository.findById(grupo.getVendedorId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el vendedor con el ID: " + grupo.getVendedorId()));

        if (vendedor.getUsuario().getId().equals(comprador.getId())) {
            throw new IllegalArgumentException("Restricción de Negocio: No puedes realizar compras en tu propia tienda.");
        }

        if (grupo.getDetalles() == null || grupo.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Cada grupo debe contener al menos un producto.");
        }

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randStr = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String numeroPedido = "MM-" + dateStr + "-" + randStr;

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(numeroPedido);
        pedido.setComprador(comprador);
        pedido.setVendedor(vendedor);
        pedido.setCostoEnvio(grupo.getCostoEnvio());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal acumuladoSubtotal = BigDecimal.ZERO;

        for (DetallePedidoRequest detReq : grupo.getDetalles()) {
            Producto producto = productoRepository.findById(detReq.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con el ID: " + detReq.getProductoId()));

            if (!producto.getVendedor().getId().equals(vendedor.getId())) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no pertenece a la tienda seleccionada.");
            }

            if (!producto.getActivo()) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no está activo para la venta.");
            }

            Inventario inventario = inventarioRepository.findByProductoId(producto.getId())
                    .orElseGet(() -> {
                        Inventario nuevo = new Inventario();
                        nuevo.setStockActual(producto.getStock());
                        nuevo.setStockMinimo(0);
                        nuevo.setProducto(producto);
                        nuevo.setUltimaActualizacion(LocalDateTime.now());
                        return inventarioRepository.save(nuevo);
                    });

            if (inventario.getStockActual() < detReq.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto '" + producto.getNombre() + "'. Stock disponible: " + inventario.getStockActual() + ", Solicitado: " + detReq.getCantidad());
            }

            int nuevoStock = inventario.getStockActual() - detReq.getCantidad();
            inventario.setStockActual(nuevoStock);
            inventario.setUltimaActualizacion(LocalDateTime.now());
            inventarioRepository.save(inventario);

            producto.setStock(nuevoStock);
            productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
            movimiento.setCantidad(detReq.getCantidad());
            movimiento.setObservacion("Despacho por creación de pedido: " + numeroPedido);
            movimiento.setInventario(inventario);
            movimientoInventarioRepository.save(movimiento);

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(detReq.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());

            BigDecimal lineaSubtotal = producto.getPrecio().multiply(BigDecimal.valueOf(detReq.getCantidad()));
            detalle.setSubtotal(lineaSubtotal);
            acumuladoSubtotal = acumuladoSubtotal.add(lineaSubtotal);
            pedido.addDetalle(detalle);
        }

        pedido.setSubtotal(acumuladoSubtotal);
        BigDecimal impuesto = acumuladoSubtotal.multiply(BigDecimal.valueOf(0.18));
        pedido.setImpuesto(impuesto);
        BigDecimal total = acumuladoSubtotal.add(impuesto).add(grupo.getCostoEnvio());
        pedido.setTotal(total);

        Pedido guardado = pedidoRepository.save(pedido);
        return mapToResponse(guardado);
    }
}
