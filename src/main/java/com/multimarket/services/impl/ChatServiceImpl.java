package com.multimarket.services.impl;

import com.multimarket.dto.ConversacionRequest;
import com.multimarket.dto.ConversacionResponse;
import com.multimarket.dto.MensajeResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.services.Interfaces.ChatService;
import com.multimarket.services.Interfaces.NotificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final VendedorRepository vendedorRepository;
    private final NotificacionService notificacionService;

    public ChatServiceImpl(ConversacionRepository conversacionRepository,
                           MensajeRepository mensajeRepository,
                           UsuarioRepository usuarioRepository,
                           VendedorRepository vendedorRepository,
                           NotificacionService notificacionService) {
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.vendedorRepository = vendedorRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    public ConversacionResponse crearConversacion(String compradorCorreo, ConversacionRequest request) {
        Usuario comprador = usuarioRepository.findByCorreo(compradorCorreo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el comprador con el correo: " + compradorCorreo));

        Vendedor vendedor = vendedorRepository.findById(request.getVendedorId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el vendedor con el ID: " + request.getVendedorId()));

        if (vendedor.getUsuario().getId().equals(comprador.getId())) {
            throw new IllegalArgumentException("No puedes iniciar un chat con tu propia tienda.");
        }

        // Si ya existe un canal entre ambos, lo retornamos para no duplicar canales de conversación
        Optional<Conversacion> existente = conversacionRepository
                .findByCompradorIdAndVendedorId(comprador.getId(), vendedor.getId());
        
        if (existente.isPresent()) {
            return mapToResponse(existente.get(), comprador);
        }

        Conversacion conversacion = new Conversacion();
        conversacion.setComprador(comprador);
        conversacion.setVendedor(vendedor);
        conversacion.setActiva(true);

        Conversacion guardada = conversacionRepository.save(conversacion);
        notificacionService.generarNotificacion(
                comprador.getId(),
                "Chat iniciado con " + vendedor.getNombreTienda(),
                "Ya puedes continuar la conversación con " + vendedor.getNombreTienda() + ".",
                TipoNotificacion.CHAT
        );
        return mapToResponse(guardada, comprador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversacionResponse> listarConversacionesPorUsuario(String usuarioCorreo) {
        Usuario usuario = usuarioRepository.findByCorreo(usuarioCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioCorreo));

        boolean esVendedor = usuario.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.VENDEDOR);

        List<Conversacion> conversaciones;
        if (esVendedor) {
            conversaciones = conversacionRepository.findByVendedorUsuarioCorreoOrderByFechaCreacionDesc(usuarioCorreo);
        } else {
            conversaciones = conversacionRepository.findByCompradorCorreoOrderByFechaCreacionDesc(usuarioCorreo);
        }

        return conversaciones.stream()
                .map(conversacion -> mapToResponse(conversacion, usuario))
                .collect(Collectors.toList());
    }

    @Override
    public List<MensajeResponse> obtenerHistorial(Long conversacionId, String usuarioCorreo) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada con el ID: " + conversacionId));

        Usuario usuario = usuarioRepository.findByCorreo(usuarioCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioCorreo));

        boolean esComprador = conversacion.getComprador().getId().equals(usuario.getId());
        boolean esVendedor = conversacion.getVendedor().getUsuario().getId().equals(usuario.getId());

        if (!esComprador && !esVendedor) {
            throw new SecurityException("Acceso Denegado: No eres participante de esta conversación.");
        }

        List<Mensaje> mensajes = mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(conversacionId);

        // Lógica de negocio: al leer el historial, marcamos como leídos los mensajes que nos enviaron a nosotros
        for (Mensaje mensaje : mensajes) {
            if (!mensaje.getRemitente().getId().equals(usuario.getId()) && !mensaje.getLeido()) {
                mensaje.setLeido(true);
                mensajeRepository.save(mensaje);
            }
        }

        return mensajes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public MensajeResponse enviarMensaje(Long conversacionId, String remitenteCorreo, String contenido) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada con el ID: " + conversacionId));

        Usuario remitente = usuarioRepository.findByCorreo(remitenteCorreo)
                .orElseThrow(() -> new IllegalArgumentException("Remitente no encontrado: " + remitenteCorreo));

        boolean esComprador = conversacion.getComprador().getId().equals(remitente.getId());
        boolean esVendedor = conversacion.getVendedor().getUsuario().getId().equals(remitente.getId());

        if (!esComprador && !esVendedor) {
            throw new SecurityException("Acceso Denegado: No perteneces a esta conversación.");
        }

        if (!conversacion.getActiva()) {
            throw new IllegalArgumentException("La conversación ha sido archivada o desactivada.");
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setContenido(contenido);
        mensaje.setRemitente(remitente);
        mensaje.setConversacion(conversacion);
        mensaje.setLeido(false);

        Mensaje guardado = mensajeRepository.save(mensaje);
        generarNotificacionChat(conversacion, remitente, guardado);
        
        return mapToResponse(guardado);
    }

    private void generarNotificacionChat(Conversacion conversacion, Usuario remitente, Mensaje mensaje) {
        Usuario destinatario;
        String remitenteNombre;
        String titulo;
        String mensajeNotificacion;

        if (conversacion.getComprador().getId().equals(remitente.getId())) {
            destinatario = conversacion.getVendedor().getUsuario();
            remitenteNombre = resolverNombreMostrar(remitente, remitente.getCorreo());
            titulo = "Nuevo mensaje de " + remitenteNombre;
            mensajeNotificacion = "El comprador " + remitenteNombre + " te escribió: " + resumirContenido(mensaje.getContenido());
        } else {
            destinatario = conversacion.getComprador();
            remitenteNombre = conversacion.getVendedor().getNombreTienda();
            titulo = "Respuesta de " + remitenteNombre;
            mensajeNotificacion = "La tienda " + remitenteNombre + " te respondió: " + resumirContenido(mensaje.getContenido());
        }

        if (destinatario == null || destinatario.getId().equals(remitente.getId())) {
            return;
        }

        notificacionService.generarNotificacion(
                destinatario.getId(),
                titulo,
                mensajeNotificacion,
                TipoNotificacion.CHAT
        );
    }

    private String resolverNombreMostrar(Usuario usuario, String fallback) {
        if (usuario != null && usuario.getPerfil() != null) {
            String nombres = usuario.getPerfil().getNombres() != null ? usuario.getPerfil().getNombres().trim() : "";
            String apellidos = usuario.getPerfil().getApellidos() != null ? usuario.getPerfil().getApellidos().trim() : "";
            String nombreCompleto = (nombres + " " + apellidos).trim();
            if (!nombreCompleto.isEmpty()) {
                return nombreCompleto;
            }
        }

        if (fallback != null && fallback.contains("@")) {
            String localPart = fallback.substring(0, fallback.indexOf('@')).replace('.', ' ').replace('_', ' ').replace('-', ' ').trim();
            if (!localPart.isEmpty()) {
                return localPart.substring(0, 1).toUpperCase() + localPart.substring(1);
            }
        }

        return fallback != null && !fallback.isBlank() ? fallback : "usuario";
    }

    private String resumirContenido(String contenido) {
        if (contenido == null) {
            return "";
        }

        String texto = contenido.trim();
        if (texto.length() <= 120) {
            return texto;
        }
        return texto.substring(0, 117).trim() + "...";
    }

    private ConversacionResponse mapToResponse(Conversacion c, Usuario usuario) {
        Mensaje ultimoMensaje = mensajeRepository.findTopByConversacionIdOrderByFechaEnvioDesc(c.getId()).orElse(null);
        Long usuarioId = usuario != null ? usuario.getId() : null;
        Integer noLeidos = usuarioId == null ? 0 : Math.toIntExact(
                mensajeRepository.countByConversacionIdAndLeidoFalseAndRemitenteIdNot(c.getId(), usuarioId)
        );

        return new ConversacionResponse(
                c.getId(),
                c.getFechaCreacion(),
                c.getActiva(),
                c.getComprador().getCorreo(),
                c.getVendedor().getId(),
                c.getVendedor().getNombreTienda(),
                ultimoMensaje != null ? ultimoMensaje.getContenido() : "",
                ultimoMensaje != null ? ultimoMensaje.getFechaEnvio() : c.getFechaCreacion(),
                noLeidos
        );
    }

    private MensajeResponse mapToResponse(Mensaje m) {
        return new MensajeResponse(
                m.getId(),
                m.getContenido(),
                m.getFechaEnvio(),
                m.getLeido(),
                m.getRemitente().getCorreo()
        );
    }
}
