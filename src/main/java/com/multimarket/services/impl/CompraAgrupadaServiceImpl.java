package com.multimarket.services.impl;

import com.multimarket.dto.CompraAgrupadaResponse;
import com.multimarket.dto.DetallePedidoResponse;
import com.multimarket.dto.PedidoResponse;
import com.multimarket.models.CompraAgrupada;
import com.multimarket.models.DetallePedido;
import com.multimarket.models.EstadoPedido;
import com.multimarket.models.Pedido;
import com.multimarket.repositories.CompraAgrupadaRepository;
import com.multimarket.services.Interfaces.CompraAgrupadaService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.awt.Color;

@Service
@Transactional(readOnly = true)
public class CompraAgrupadaServiceImpl implements CompraAgrupadaService {

    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_OBLIQUE = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 38f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-PE"));
    private static final Color BG = new Color(248, 250, 252);
    private static final Color SURFACE = new Color(255, 255, 255);
    private static final Color SURFACE_2 = new Color(249, 251, 255);
    private static final Color SURFACE_3 = new Color(243, 246, 251);
    private static final Color BORDER = new Color(217, 224, 233);
    private static final Color TEXT = new Color(17, 24, 39);
    private static final Color MUTED = new Color(96, 108, 128);
    private static final Color PRIMARY = new Color(135, 98, 255);
    private static final Color PRIMARY_SOFT = new Color(105, 78, 214);
    private static final Color SUCCESS = new Color(41, 198, 132);
    private static final Color GOLD = new Color(241, 190, 72);
    private static final Color ERROR = new Color(241, 97, 115);
    private static final Color WHITE = new Color(255, 255, 255);

    private final CompraAgrupadaRepository compraAgrupadaRepository;

    public CompraAgrupadaServiceImpl(CompraAgrupadaRepository compraAgrupadaRepository) {
        this.compraAgrupadaRepository = compraAgrupadaRepository;
    }

    @Override
    public List<CompraAgrupadaResponse> listarComprasPorComprador(String compradorCorreo) {
        return compraAgrupadaRepository.findByCompradorCorreoOrderByFechaCompraDesc(compradorCorreo)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CompraAgrupadaResponse consultarCompra(Long id, String compradorCorreo) {
        CompraAgrupada compra = compraAgrupadaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la compra agrupada con el ID: " + id));

        if (!compra.getComprador().getCorreo().equals(compradorCorreo)) {
            throw new SecurityException("Acceso Denegado: No puedes consultar una compra que no te pertenece.");
        }

        return mapToResponse(compra);
    }

    @Override
    public byte[] generarBoletaPdf(Long id, String compradorCorreo) {
        CompraAgrupadaResponse compra = consultarCompra(id, compradorCorreo);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                paintBaseBackground(content);

                float y = PAGE_HEIGHT - 42f;
                y = drawHeroHeader(content, compra, y);
                y -= 18f;
                y = drawSummaryGrid(content, compra, y);
                y -= 14f;
                y = drawFinancialSummary(content, compra, y);
                y -= 74f;
                y = drawSectionTitle(content, "Pedidos por tienda", "Cada vendedor recibe su pedido separado", y);
                y -= 8f;

                for (PedidoResponse pedido : compra.getPedidos()) {
                    y = drawOrderCard(content, pedido, y);
                    y -= 12f;
                }

                y = drawFooter(content, y);
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la compra.", ex);
        }
    }

    private void paintBaseBackground(PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(BG);
        content.addRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
        content.fill();
    }

    private float drawHeroHeader(PDPageContentStream content, CompraAgrupadaResponse compra, float topY) throws IOException {
        float height = 112f;
        float left = MARGIN;
        float bottom = topY - height;

        drawRoundedPanel(content, left, bottom, CONTENT_WIDTH, height, SURFACE, BORDER);
        drawRoundedPanel(content, left + 1.5f, bottom + 1.5f, CONTENT_WIDTH - 3f, height - 3f, SURFACE_2, null);

        content.setNonStrokingColor(PRIMARY);
        content.addRect(left, topY - 14f, CONTENT_WIDTH, 4f);
        content.fill();

        content.setNonStrokingColor(PRIMARY_SOFT);
        content.addRect(left + 18f, topY - 78f, 46f, 46f);
        content.fill();

        content.beginText();
        content.setFont(FONT_BOLD, 17f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(left + 32f, topY - 50f);
        content.showText("MM");
        content.endText();

        content.beginText();
        content.setFont(FONT_BOLD, 20f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(left + 82f, topY - 38f);
        content.showText("Boleta de Compra");
        content.endText();

        content.beginText();
        content.setFont(FONT_REGULAR, 9.3f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(left + 82f, topY - 56f);
        content.showText("Comprobante premium alineado al checkout y al sistema marketplace.");
        content.endText();

        drawMetricChip(content, left + CONTENT_WIDTH - 188f, topY - 34f, 158f, 28f, "Compra", compra.getNumeroCompra(), PRIMARY);
        drawMetricChip(content, left + CONTENT_WIDTH - 188f, topY - 68f, 158f, 28f, "Estado", compra.getEstadoGeneral(), successColor(compra.getEstadoGeneral()));

        return bottom;
    }

    private float drawSummaryGrid(PDPageContentStream content, CompraAgrupadaResponse compra, float topY) throws IOException {
        float gap = 10f;
        float cardWidth = (CONTENT_WIDTH - (gap * 3)) / 4f;
        float cardHeight = 70f;
        float bottom = topY - cardHeight;

        drawInfoCard(content, MARGIN, bottom, cardWidth, cardHeight, "Fecha", formatDate(compra.getFechaCompra()));
        drawInfoCard(content, MARGIN + cardWidth + gap, bottom, cardWidth, cardHeight, "Pago", compra.getMetodoPago());
        drawInfoCard(content, MARGIN + (cardWidth + gap) * 2, bottom, cardWidth, cardHeight, "Tienda(s)", String.valueOf(compra.getPedidos().size()));
        drawInfoCard(content, MARGIN + (cardWidth + gap) * 3, bottom, cardWidth, cardHeight, "Comprador", compra.getPedidos().isEmpty() ? "Cliente" : compra.getPedidos().get(0).getCompradorCorreo());

        return bottom;
    }

    private float drawFinancialSummary(PDPageContentStream content, CompraAgrupadaResponse compra, float topY) throws IOException {
        float height = 136f;
        float bottom = topY - height;

        drawRoundedPanel(content, MARGIN, bottom, CONTENT_WIDTH, height, SURFACE, BORDER);
        content.setNonStrokingColor(PRIMARY);
        content.addRect(MARGIN, topY - 12f, CONTENT_WIDTH, 4f);
        content.fill();

        content.beginText();
        content.setFont(FONT_BOLD, 12.0f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(MARGIN + 16f, topY - 26f);
        content.showText("Resumen de Facturación");
        content.endText();

        content.beginText();
        content.setFont(FONT_REGULAR, 8.5f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(MARGIN + 16f, topY - 39f);
        content.showText("Valores calculados en base a los pedidos separados por tienda.");
        content.endText();

        float labelY = topY - 48f;
        drawSummaryLine(content, "Subtotal", compra.getSubtotal(), labelY);
        drawSummaryLine(content, "IGV", compra.getImpuesto(), labelY - 12f);
        drawSummaryLine(content, "Envío total", compra.getCostoEnvioTotal(), labelY - 24f);
        drawSummaryLine(content, "Importe total", compra.getTotal(), labelY - 36f, true);

        return bottom;
    }

    private float drawSectionTitle(PDPageContentStream content, String title, String subtitle, float topY) throws IOException {
        content.beginText();
        content.setFont(FONT_BOLD, 13.5f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(MARGIN, topY);
        content.showText(title);
        content.endText();

        content.beginText();
        content.setFont(FONT_REGULAR, 9.2f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(MARGIN, topY - 12f);
        content.showText(subtitle);
        content.endText();

        return topY - 30f;
    }

    private float drawOrderCard(PDPageContentStream content, PedidoResponse pedido, float topY) throws IOException {
        float headerHeight = 44f;
        float detailsHeight = Math.max(48f, 16f + (pedido.getDetalles().size() * 16f));
        float summaryHeight = 40f;
        float cardHeight = headerHeight + detailsHeight + summaryHeight + 16f;
        float bottom = topY - cardHeight;

        if (bottom < 42f) {
            return topY;
        }

        drawRoundedPanel(content, MARGIN, bottom, CONTENT_WIDTH, cardHeight, SURFACE_2, BORDER);

        content.setNonStrokingColor(PRIMARY_SOFT);
        content.addRect(MARGIN, topY - 44f, CONTENT_WIDTH, 44f);
        content.fill();

        content.setNonStrokingColor(SUCCESS);
        content.addRect(MARGIN, topY - 44f, 5f, 44f);
        content.fill();

        content.beginText();
        content.setFont(FONT_BOLD, 11.3f);
        setTextColor(content, WHITE);
        content.newLineAtOffset(MARGIN + 16f, topY - 17f);
        content.showText(truncateText(pedido.getVendedorTienda(), 34));
        content.endText();

        content.beginText();
        content.setFont(FONT_REGULAR, 8.2f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(MARGIN + 16f, topY - 32f);
        content.showText("Pedido " + pedido.getNumeroPedido() + "  •  " + pedido.getEstado());
        content.endText();

        drawBadge(content, PAGE_WIDTH - MARGIN - 118f, topY - 30f, 100f, 18f, pedido.getEstado(), successColor(pedido.getEstado()));

        // Details header
        float y = topY - 68f;
        content.beginText();
        content.setFont(FONT_BOLD, 9.4f);
        setTextColor(content, PRIMARY);
        content.newLineAtOffset(MARGIN + 16f, y);
        content.showText("Productos");
        content.endText();

        y -= 15f;
        for (DetallePedidoResponse detalle : pedido.getDetalles()) {
            y = drawWrappedItemLine(content, detalle, y);
        }

        y -= 6f;
        drawThinSeparator(content, MARGIN + 16f, y, CONTENT_WIDTH - 32f);
        y -= 10f;

        float summaryY = y;
        drawSummaryLine(content, "Subtotal tienda", pedido.getSubtotal(), summaryY);
        drawSummaryLine(content, "Envío tienda", pedido.getCostoEnvio(), summaryY - 14f);
        drawSummaryLine(content, "Total tienda", pedido.getTotal(), summaryY - 32f, true);

        return bottom;
    }

    private float drawWrappedItemLine(PDPageContentStream content, DetallePedidoResponse detalle, float topY) throws IOException {
        String title = detalle.getProductoNombre() + " x" + detalle.getCantidad();
        String amount = "S/ " + detalle.getSubtotal().setScale(2, RoundingMode.HALF_UP).toPlainString();
        float left = MARGIN + 18f;
        float right = PAGE_WIDTH - MARGIN - 18f;

        content.beginText();
        content.setFont(FONT_REGULAR, 8.8f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(left, topY);
        content.showText(truncateText(title, 64));
        content.endText();

        content.beginText();
        content.setFont(FONT_BOLD, 8.9f);
        setTextColor(content, GOLD);
        content.newLineAtOffset(right - textWidth(FONT_BOLD, 9.2f, amount), topY);
        content.showText(amount);
        content.endText();

        content.beginText();
        content.setFont(FONT_REGULAR, 7.8f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(left, topY - 11f);
        content.showText("SKU " + detalle.getProductoId() + "  •  P.U. S/ " + detalle.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP).toPlainString());
        content.endText();

        return topY - 24f;
    }

    private void drawSummaryLine(PDPageContentStream content, String label, BigDecimal value, float y) throws IOException {
        drawSummaryLine(content, label, value, y, false);
    }

    private void drawSummaryLine(PDPageContentStream content, String label, BigDecimal value, float y, boolean highlight) throws IOException {
        content.beginText();
        content.setFont(highlight ? FONT_BOLD : FONT_REGULAR, highlight ? 10.0f : 9.2f);
        setTextColor(content, highlight ? TEXT : MUTED);
        content.newLineAtOffset(MARGIN + 18f, y);
        content.showText(label);
        content.endText();

        String formatted = "S/ " + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
        content.beginText();
        content.setFont(highlight ? FONT_BOLD : FONT_REGULAR, 10.0f);
        setTextColor(content, highlight ? PRIMARY_SOFT : TEXT);
        content.newLineAtOffset(PAGE_WIDTH - MARGIN - 18f - textWidth(highlight ? FONT_BOLD : FONT_REGULAR, 10.0f, formatted), y);
        content.showText(formatted);
        content.endText();
    }

    private float drawFooter(PDPageContentStream content, float topY) throws IOException {
        float footerTop = Math.max(42f, topY);
        drawThinSeparator(content, MARGIN, footerTop + 10f, CONTENT_WIDTH);

        content.beginText();
        content.setFont(FONT_OBLIQUE, 8.4f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(MARGIN, footerTop - 4f);
        content.showText("Gracias por comprar en MultiMarket. Conserva esta boleta para consultas o seguimiento.");
        content.endText();

        content.beginText();
        content.setFont(FONT_BOLD, 8.2f);
        setTextColor(content, SUCCESS);
        content.newLineAtOffset(PAGE_WIDTH - MARGIN - 112f, footerTop - 4f);
        content.showText("Comprobante seguro");
        content.endText();

        return footerTop - 20f;
    }

    private void drawInfoCard(PDPageContentStream content, float x, float y, float w, float h, String label, String value) throws IOException {
        drawRoundedPanel(content, x, y, w, h, SURFACE_3, BORDER);

        content.setNonStrokingColor(PRIMARY);
        content.addRect(x, y + h - 4f, w, 4f);
        content.fill();

        content.beginText();
        content.setFont(FONT_BOLD, 7.8f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(x + 10f, y + h - 16f);
        content.showText(label.toUpperCase(Locale.ROOT));
        content.endText();

        content.beginText();
        content.setFont(FONT_BOLD, 9.7f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(x + 10f, y + 16f);
        content.showText(truncateText(value, 26));
        content.endText();
    }

    private void drawMetricChip(PDPageContentStream content, float x, float y, float w, float h, String label, String value, Color accent) throws IOException {
        drawRoundedPanel(content, x, y - h, w, h, SURFACE_3, BORDER);
        content.setNonStrokingColor(accent);
        content.addRect(x, y - h, 4f, h);
        content.fill();

        content.beginText();
        content.setFont(FONT_REGULAR, 7.2f);
        setTextColor(content, MUTED);
        content.newLineAtOffset(x + 10f, y - 9f);
        content.showText(label.toUpperCase(Locale.ROOT));
        content.endText();

        content.beginText();
        content.setFont(FONT_BOLD, 8.5f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(x + 10f, y - 20f);
        content.showText(truncateText(value, 18));
        content.endText();
    }

    private void drawBadge(PDPageContentStream content, float x, float y, float w, float h, String value, Color accent) throws IOException {
        drawRoundedPanel(content, x, y - h, w, h, SURFACE_3, accent);
        content.setNonStrokingColor(accent);
        content.addRect(x, y - h, 4f, h);
        content.fill();

        content.beginText();
        content.setFont(FONT_BOLD, 7.6f);
        setTextColor(content, TEXT);
        content.newLineAtOffset(x + 10f, y - 11f);
        content.showText(truncateText(value, 18));
        content.endText();
    }

    private void drawRoundedPanel(PDPageContentStream content, float x, float y, float w, float h, Color fill, Color border) throws IOException {
        content.setNonStrokingColor(fill);
        content.addRect(x, y, w, h);
        content.fill();
        if (border != null) {
            content.setStrokingColor(border);
            content.addRect(x, y, w, h);
            content.stroke();
        }
    }

    private void drawThinSeparator(PDPageContentStream content, float x, float y, float w) throws IOException {
        content.setStrokingColor(BORDER);
        content.moveTo(x, y);
        content.lineTo(x + w, y);
        content.stroke();
    }

    private void ensurePageSpace(PDPageContentStream content, float bottom) {
        // Future improvement: paginate if orders exceed one page.
    }

    private void setTextColor(PDPageContentStream content, Color color) throws IOException {
        content.setNonStrokingColor(color);
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/D";
        }
        return dateTime.format(DATE_FMT);
    }

    private String truncateText(String text, int maxChars) {
        if (text == null) return "";
        String normalized = text.trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    private float textWidth(PDType1Font font, float size, String text) throws IOException {
        if (text == null || text.isEmpty()) return 0f;
        return (font.getStringWidth(text) / 1000f) * size;
    }

    private Color successColor(String estado) {
        if (estado == null) return PRIMARY;
        return switch (estado.toUpperCase(Locale.ROOT)) {
            case "PAGADO", "ENTREGADO" -> SUCCESS;
            case "CANCELADO" -> ERROR;
            case "ENVIADO" -> GOLD;
            default -> PRIMARY;
        };
    }

    private CompraAgrupadaResponse mapToResponse(CompraAgrupada compra) {
        List<PedidoResponse> pedidos = compra.getPedidos().stream()
                .map(this::mapPedido)
                .collect(Collectors.toList());

        return new CompraAgrupadaResponse(
                compra.getId(),
                compra.getNumeroCompra(),
                compra.getFechaCompra(),
                compra.getMetodoPago().name(),
                calcularEstadoGeneral(compra.getPedidos()),
                pedidos,
                compra.getSubtotal(),
                compra.getImpuesto(),
                compra.getCostoEnvioTotal(),
                compra.getTotal()
        );
    }

    private PedidoResponse mapPedido(Pedido ped) {
        List<DetallePedidoResponse> detallesDto = ped.getDetalles().stream()
                .map(this::mapDetalle)
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

    private DetallePedidoResponse mapDetalle(DetallePedido detalle) {
        return new DetallePedidoResponse(
                detalle.getId(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal(),
                detalle.getProducto().getId(),
                detalle.getProducto().getNombre()
        );
    }

    private String calcularEstadoGeneral(List<Pedido> pedidos) {
        boolean todosEntregados = pedidos.stream().allMatch(p -> p.getEstado() == EstadoPedido.ENTREGADO);
        boolean todosCancelados = pedidos.stream().allMatch(p -> p.getEstado() == EstadoPedido.CANCELADO);
        boolean algunoEnviado = pedidos.stream().anyMatch(p -> p.getEstado() == EstadoPedido.ENVIADO);
        boolean todosPagados = pedidos.stream().allMatch(p -> p.getEstado() == EstadoPedido.PAGADO);

        if (todosEntregados) return "ENTREGADO";
        if (todosCancelados) return "CANCELADO";
        if (algunoEnviado) return "ENVIADO";
        if (todosPagados) return "PAGADO";
        return "PENDIENTE";
    }
}
