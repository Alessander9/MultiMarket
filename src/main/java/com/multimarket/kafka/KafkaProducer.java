package com.multimarket.kafka;

import com.multimarket.kafka.events.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class KafkaProducer {

    private static final Logger LOGGER = Logger.getLogger(KafkaProducer.class.getName());
    private final ApplicationEventPublisher eventPublisher;

    public KafkaProducer(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void sendUsuarioEvent(UsuarioRegistradoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-PRODUCER] Publicando en tópico 'usuarios-topic': {0} (ID: {1})",
                new Object[]{event.getCorreo(), event.getUsuarioId()});
        eventPublisher.publishEvent(event);
    }

    public void sendPedidoEvent(PedidoCreadoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-PRODUCER] Publicando en tópico 'pedidos-topic': Pedido {0} por {1} (Total: {2})",
                new Object[]{event.getNumeroPedido(), event.getCompradorCorreo(), event.getTotal()});
        eventPublisher.publishEvent(event);
    }

    public void sendPagoEvent(PagoConfirmadoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-PRODUCER] Publicando en tópico 'pagos-topic': Pago Aprobado {0} (Pedido ID: {1})",
                new Object[]{event.getCodigoOperacion(), event.getPedidoId()});
        eventPublisher.publishEvent(event);
    }

    public void sendInventarioEvent(StockActualizadoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-PRODUCER] Publicando en tópico 'inventario-topic': Producto ID {0} - Stock: {1} -> {2} ({3})",
                new Object[]{event.getProductoId(), event.getStockAnterior(), event.getStockNuevo(), event.getTipoMovimiento()});
        eventPublisher.publishEvent(event);
    }

    public void sendLogEvent(LogGeneradoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-PRODUCER] Publicando en tópico 'logs-topic': [{0}] {1} - {2}",
                new Object[]{event.getNivel(), event.getModulo(), event.getAccion()});
        eventPublisher.publishEvent(event);
    }

    public void sendNotificacionEvent(NotificacionGeneradaEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-PRODUCER] Publicando en tópico 'notificaciones-topic': Para Usuario ID {0} - '{1}'",
                new Object[]{event.getUsuarioId(), event.getTitulo()});
        eventPublisher.publishEvent(event);
    }
}
