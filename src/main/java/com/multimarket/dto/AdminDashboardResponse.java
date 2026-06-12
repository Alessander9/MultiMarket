package com.multimarket.dto;

import java.util.List;

public record AdminDashboardResponse(
        List<KpiCard> kpis,
        List<Long> salesData,
        List<Long> ordersData,
        List<TopCategory> topCategories,
        List<TopVendor> topVendors,
        List<RecentActivity> recentActivities,
        CriticalAlerts criticalAlerts,
        KafkaStatus kafkaStatus,
        SoapStatus soapStatus,
        LogsSummary logsSummary,
        SystemStatus systemStatus
) {
    public record KpiCard(String id, String titulo, String valor, String icono, String tendencia, boolean tendenciaPositiva) {}
    public record TopCategory(String nombre, long ventas, long pedidos, long participacion) {}
    public record TopVendor(String nombreTienda, String region, long ventas, long productosCount) {}
    public record RecentActivity(String hora, String usuario, String accion, String modulo, String resultado) {}
    public record CriticalAlerts(long stockBajo, long pedidosPendientes, long pagosFallidos, long erroresCriticos, long serviciosCaidos) {}
    public record KafkaStatus(String status, long mensajesHoy, long eventosProcesados, long errores) {}
    public record SoapStatus(String status, long transaccionesHoy, long erroresSoap, String tiempoRespuesta) {}
    public record LogsSummary(long info, long warn, long error, long fatal) {}
    public record SystemStatus(long cpu, String ram, long disco, long microservicios) {}
}
