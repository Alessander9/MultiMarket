package com.multimarket.kafka;

import com.multimarket.kafka.events.*;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class KafkaConsumer {

    private static final Logger LOGGER = Logger.getLogger(KafkaConsumer.class.getName());

    private final LogSistemaRepository logRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public KafkaConsumer(LogSistemaRepository logRepository,
                         NotificacionRepository notificacionRepository,
                         UsuarioRepository usuarioRepository) {
        this.logRepository = logRepository;
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Async
    @EventListener
    public void listenUsuarioRegistrado(UsuarioRegistradoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Consumiendo de 'usuarios-topic': {0}", event.getCorreo());
        
        // Simular lógica: Generar una notificación automática de bienvenida al registrarse
        Notificacion notif = new Notificacion();
        Usuario usr = usuarioRepository.findById(event.getUsuarioId()).orElse(null);
        if (usr != null) {
            notif.setUsuario(usr);
            notif.setTitulo("¡Bienvenido a MultiMarket!");
            notif.setMensaje("Hola " + event.getNombres() + ", tu cuenta ha sido creada exitosamente.");
            notif.setTipo(TipoNotificacion.SISTEMA);
            notif.setLeida(false);
            notif.setFechaCreacion(LocalDateTime.now());
            notificacionRepository.save(notif);
            LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Notificación de bienvenida generada y persistida.");
        }
    }

    @Async
    @EventListener
    public void listenPedidoCreado(PedidoCreadoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Consumiendo de 'pedidos-topic': Pedido {0}", event.getNumeroPedido());
        
        // Generar notificación al comprador de que su pedido ha sido registrado como PENDIENTE
        Notificacion notif = new Notificacion();
        Usuario usr = usuarioRepository.findByCorreo(event.getCompradorCorreo()).orElse(null);
        if (usr != null) {
            notif.setUsuario(usr);
            notif.setTitulo("Pedido Registrado");
            notif.setMensaje("Tu pedido " + event.getNumeroPedido() + " ha sido creado. Monto total: S/ " + event.getTotal());
            notif.setTipo(TipoNotificacion.PEDIDO);
            notif.setLeida(false);
            notif.setFechaCreacion(LocalDateTime.now());
            notificacionRepository.save(notif);
            LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Notificación de pedido creado enviada al comprador.");
        }
    }

    @Async
    @EventListener
    public void listenPagoConfirmado(PagoConfirmadoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Consumiendo de 'pagos-topic': Pago Aprobado para Pedido ID {0}", event.getPedidoId());
        
        // Simular lógica: Generar notificación de pago aprobado
        // Buscamos el usuario asociado al pago (comprador o vendedor)
        // Por simplicidad, mandaremos la notificación a todos los involucrados o al comprador si podemos obtenerlo
        // Aquí simulamos guardarla para el comprador
        // En una implementación real, tendríamos el compradorId directo
    }

    @Async
    @EventListener
    public void listenStockActualizado(StockActualizadoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Consumiendo de 'inventario-topic': Stock modificado para producto {0}", event.getProductoId());
        
        // Si el stock nuevo es menor al mínimo, generar alerta al vendedor
        if (event.getStockNuevo() < 5) { // Threshold de alerta
            LOGGER.log(Level.WARNING, "[KAFKA-CONSUMER] ALERTA: ¡Stock bajo del producto ID {0}! Stock actual: {1}", 
                    new Object[]{event.getProductoId(), event.getStockNuevo()});
        }
    }

    @Async
    @EventListener
    public void listenLogGenerado(LogGeneradoEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Consumiendo de 'logs-topic' para persistir en BD");
        
        LogSistema log = new LogSistema();
        log.setNivel(NivelLog.valueOf(event.getNivel()));
        log.setModulo(ModuloSistema.valueOf(event.getModulo()));
        log.setAccion(event.getAccion());
        log.setDescripcion(event.getDescripcion());
        log.setIpOrigen(event.getIpOrigen());
        log.setEndpoint(event.getEndpoint());
        log.setMetodoHttp(event.getMetodoHttp());
        log.setUsuarioId(event.getUsuarioId());
        log.setRequestData(event.getRequestData());
        log.setResponseData(event.getResponseData());
        log.setStackTrace(event.getStackTrace());
        log.setExitoso(event.getExitoso());
        log.setFechaHora(event.getFechaHora());
        
        logRepository.save(log);
        LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Log persistido en base de datos con éxito.");
    }

    @Async
    @EventListener
    public void listenNotificacionGenerada(NotificacionGeneradaEvent event) {
        LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Consumiendo de 'notificaciones-topic' para persistir en BD");
        
        Usuario usr = usuarioRepository.findById(event.getUsuarioId()).orElse(null);
        if (usr != null) {
            Notificacion notif = new Notificacion();
            notif.setUsuario(usr);
            notif.setTitulo(event.getTitulo());
            notif.setMensaje(event.getMensaje());
            notif.setTipo(TipoNotificacion.valueOf(event.getTipo()));
            notif.setLeida(false);
            notif.setFechaCreacion(event.getFechaCreacion());
            
            notificacionRepository.save(notif);
            LOGGER.log(Level.INFO, "[KAFKA-CONSUMER] Notificación de tipo {0} guardada en BD.", event.getTipo());
        }
    }
}
