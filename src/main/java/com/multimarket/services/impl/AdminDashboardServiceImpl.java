package com.multimarket.services.impl;

import com.multimarket.dto.AdminDashboardResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl {

    private final UsuarioRepository usuarioRepository;
    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final ConversacionRepository conversacionRepository;
    private final InventarioRepository inventarioRepository;
    private final LogSistemaRepository logSistemaRepository;
    private final TransaccionSOAPRepository transaccionSOAPRepository;

    public AdminDashboardServiceImpl(UsuarioRepository usuarioRepository,
                                     VendedorRepository vendedorRepository,
                                     ProductoRepository productoRepository,
                                     PedidoRepository pedidoRepository,
                                     PagoRepository pagoRepository,
                                     ConversacionRepository conversacionRepository,
                                     InventarioRepository inventarioRepository,
                                     LogSistemaRepository logSistemaRepository,
                                     TransaccionSOAPRepository transaccionSOAPRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vendedorRepository = vendedorRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.conversacionRepository = conversacionRepository;
        this.inventarioRepository = inventarioRepository;
        this.logSistemaRepository = logSistemaRepository;
        this.transaccionSOAPRepository = transaccionSOAPRepository;
    }

    public AdminDashboardResponse getSummary() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<Producto> productos = productoRepository.findAll();
        List<Vendedor> vendedores = vendedorRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Pago> pagos = pagoRepository.findAll();
        List<Conversacion> conversaciones = conversacionRepository.findAll();
        List<Inventario> inventarios = inventarioRepository.findAll();
        List<LogSistema> logs = logSistemaRepository.findAll();
        List<TransaccionSOAP> soapTransactions = transaccionSOAPRepository.findAll();

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        long usuariosTotales = usuarios.size();
        long vendedoresActivos = vendedores.stream().filter(v -> Boolean.TRUE.equals(v.getActivo())).count();
        long productosPublicados = productos.stream().filter(p -> Boolean.TRUE.equals(p.getActivo())).count();
        long pedidosHoy = pedidos.stream().filter(p -> sameDay(p.getFechaPedido(), today)).count();
        long ventasHoy = sumCurrency(pedidos.stream().filter(p -> sameDay(p.getFechaPedido(), today)).map(Pedido::getTotal).toList());
        long ventasMes = sumCurrency(pedidos.stream().filter(p -> !p.getFechaPedido().toLocalDate().isBefore(firstDayOfMonth)).map(Pedido::getTotal).toList());
        long pagosPendientes = pagos.stream().filter(p -> p.getEstadoPago() == EstadoPago.PENDIENTE).count();
        long conversacionesActivas = conversaciones.stream().filter(c -> Boolean.TRUE.equals(c.getActiva())).count();

        List<Long> salesData = IntStream.rangeClosed(6, 0)
                .mapToObj(offset -> today.minusDays(offset))
                .map(day -> sumCurrency(pedidos.stream()
                        .filter(p -> sameDay(p.getFechaPedido(), day))
                        .map(Pedido::getTotal)
                        .toList()))
                .toList();

        List<Long> ordersData = IntStream.rangeClosed(6, 0)
                .mapToObj(offset -> today.minusDays(offset))
                .map(day -> pedidos.stream().filter(p -> sameDay(p.getFechaPedido(), day)).count())
                .toList();

        Map<String, CategoryAgg> categoryAgg = pedidos.stream()
                .flatMap(p -> p.getDetalles().stream())
                .collect(Collectors.toMap(
                        detalle -> detalle.getProducto().getCategoria().getNombre(),
                        detalle -> new CategoryAgg(
                                detalle.getSubtotal().longValue(),
                                1L
                        ),
                        (a, b) -> new CategoryAgg(a.ventas() + b.ventas(), a.pedidos() + b.pedidos())
                ));

        List<AdminDashboardResponse.TopCategory> topCategories = categoryAgg.entrySet().stream()
                .sorted(Map.Entry.<String, CategoryAgg>comparingByValue(Comparator.comparingLong(CategoryAgg::ventas)).reversed())
                .map(entry -> new AdminDashboardResponse.TopCategory(
                        entry.getKey(),
                        entry.getValue().ventas(),
                        entry.getValue().pedidos(),
                        ventasHoy > 0 ? Math.round((entry.getValue().ventas() * 100.0) / ventasHoy) : 0
                ))
                .toList();

        Map<Long, VendorAgg> vendorAgg = pedidos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getVendedor().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            Vendedor vendedor = list.get(0).getVendedor();
                            long ventas = sumCurrency(list.stream().map(Pedido::getTotal).toList());
                            long productosCount = productos.stream().filter(prod -> prod.getVendedor().getId().equals(vendedor.getId())).count();
                            return new VendorAgg(vendedor.getNombreTienda(), vendedor.getRegion(), ventas, productosCount);
                        })
                ));

        List<AdminDashboardResponse.TopVendor> topVendors = vendorAgg.entrySet().stream()
                .sorted(Map.Entry.<Long, VendorAgg>comparingByValue(Comparator.comparingLong(VendorAgg::ventas)).reversed())
                .limit(10)
                .map(entry -> new AdminDashboardResponse.TopVendor(
                        entry.getValue().nombreTienda(),
                        entry.getValue().region(),
                        entry.getValue().ventas(),
                        entry.getValue().productosCount()
                ))
                .toList();

        List<AdminDashboardResponse.RecentActivity> recentActivities = logs.stream()
                .sorted(Comparator.comparing(LogSistema::getFechaHora).reversed())
                .limit(8)
                .map(log -> new AdminDashboardResponse.RecentActivity(
                        log.getFechaHora() != null ? log.getFechaHora().toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString() : "--:--",
                        resolveUsername(log),
                        log.getAccion(),
                        log.getModulo() != null ? log.getModulo().name() : "SISTEMA",
                        Boolean.TRUE.equals(log.getExitoso()) ? "OK" : "ERROR"
                ))
                .toList();

        long stockBajo = inventarios.stream().filter(inv -> inv.getStockActual() <= inv.getStockMinimo()).count();
        long pedidosPendientesCount = pedidos.stream().filter(p -> p.getEstado() == EstadoPedido.PENDIENTE).count();
        long pagosFallidos = pagos.stream().filter(p -> p.getEstadoPago() == EstadoPago.RECHAZADO).count();
        long erroresCriticos = logs.stream().filter(log -> log.getNivel() == NivelLog.ERROR || log.getNivel() == NivelLog.FATAL).count();

        long kafkaMensajesHoy = logs.stream()
                .filter(log -> log.getModulo() == ModuloSistema.KAFKA && sameDay(log.getFechaHora(), today))
                .count();
        long kafkaProcesados = logs.stream()
                .filter(log -> log.getModulo() == ModuloSistema.KAFKA && Boolean.TRUE.equals(log.getExitoso()))
                .count();
        long kafkaErrores = logs.stream()
                .filter(log -> log.getModulo() == ModuloSistema.KAFKA && !Boolean.TRUE.equals(log.getExitoso()))
                .count();

        long soapHoy = soapTransactions.stream().filter(tx -> sameDay(tx.getFecha(), today)).count();
        long soapErrores = soapTransactions.stream()
                .filter(tx -> tx.getEstado() != null && tx.getEstado().toUpperCase(Locale.ROOT).contains("RECHAZ"))
                .count();

        long info = logs.stream().filter(log -> log.getNivel() == NivelLog.INFO).count();
        long warn = logs.stream().filter(log -> log.getNivel() == NivelLog.WARN).count();
        long error = logs.stream().filter(log -> log.getNivel() == NivelLog.ERROR).count();
        long fatal = logs.stream().filter(log -> log.getNivel() == NivelLog.FATAL).count();

        return new AdminDashboardResponse(
                List.of(
                        new AdminDashboardResponse.KpiCard("1", "Usuarios Totales", String.valueOf(usuariosTotales), "person", "+BD real", true),
                        new AdminDashboardResponse.KpiCard("2", "Vendedores Activos", String.valueOf(vendedoresActivos), "storefront", "+BD real", true),
                        new AdminDashboardResponse.KpiCard("3", "Productos Publicados", String.valueOf(productosPublicados), "inventory_2", "+BD real", true),
                        new AdminDashboardResponse.KpiCard("4", "Pedidos del Día", String.valueOf(pedidosHoy), "shopping_bag", "actualizado hoy", true),
                        new AdminDashboardResponse.KpiCard("5", "Ventas del Día", formatCurrency(ventasHoy), "payments", "calculado desde pedidos", true),
                        new AdminDashboardResponse.KpiCard("6", "Ventas del Mes", formatCurrency(ventasMes), "analytics", "calculado desde pedidos", true),
                        new AdminDashboardResponse.KpiCard("7", "Pagos Pendientes", String.valueOf(pagosPendientes), "credit_card", "calculado desde pagos", false),
                        new AdminDashboardResponse.KpiCard("8", "Conversaciones Activas", String.valueOf(conversacionesActivas), "chat", "calculado desde chat", true)
                ),
                salesData,
                ordersData,
                topCategories,
                topVendors,
                recentActivities,
                new AdminDashboardResponse.CriticalAlerts(stockBajo, pedidosPendientesCount, pagosFallidos, erroresCriticos, 0),
                new AdminDashboardResponse.KafkaStatus("ONLINE", kafkaMensajesHoy, kafkaProcesados, kafkaErrores),
                new AdminDashboardResponse.SoapStatus("DISPONIBLE", soapHoy, soapErrores, "N/D"),
                new AdminDashboardResponse.LogsSummary(info, warn, error, fatal),
                new AdminDashboardResponse.SystemStatus(
                        readCpuLoad(),
                        formatRamUsage(),
                        readDiskUsagePercent(),
                        1
                )
        );
    }

    private boolean sameDay(LocalDateTime value, LocalDate date) {
        return value != null && value.toLocalDate().isEqual(date);
    }

    private long sumCurrency(List<BigDecimal> amounts) {
        return amounts.stream()
                .filter(amount -> amount != null)
                .mapToLong(amount -> amount.setScale(0, RoundingMode.HALF_UP).longValue())
                .sum();
    }

    private String formatCurrency(long amount) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("es", "PE"));
        format.setGroupingUsed(true);
        return "S/ " + format.format(amount);
    }

    private String resolveUsername(LogSistema log) {
        if (log.getUsuarioId() == null) {
            return "Sistema";
        }
        return usuarioRepository.findById(log.getUsuarioId())
                .map(Usuario::getCorreo)
                .orElse("Usuario #" + log.getUsuarioId());
    }

    private long readCpuLoad() {
        try {
            var osBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
            if (osBean == null) {
                return 0;
            }
            double load = osBean.getSystemCpuLoad();
            if (load < 0) {
                return 0;
            }
            return Math.round(load * 100);
        } catch (Exception ex) {
            return 0;
        }
    }

    private String formatRamUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        double gb = used / 1024d / 1024d / 1024d;
        return String.format(Locale.US, "%.1f GB", gb);
    }

    private long readDiskUsagePercent() {
        try {
            Path path = Path.of(System.getProperty("user.dir"));
            FileStore store = Files.getFileStore(path);
            long total = store.getTotalSpace();
            long used = total - store.getUsableSpace();
            if (total <= 0) {
                return 0;
            }
            return Math.round((used * 100.0) / total);
        } catch (Exception ex) {
            return 0;
        }
    }

    private record CategoryAgg(long ventas, long pedidos) {}
    private record VendorAgg(String nombreTienda, String region, long ventas, long productosCount) {}
}
