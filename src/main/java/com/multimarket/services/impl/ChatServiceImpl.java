package com.multimarket.services.impl;

import com.multimarket.dto.ConversacionRequest;
import com.multimarket.dto.ConversacionResponse;
import com.multimarket.dto.MensajeResponse;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.services.Interfaces.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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

    public ChatServiceImpl(ConversacionRepository conversacionRepository,
                           MensajeRepository mensajeRepository,
                           UsuarioRepository usuarioRepository,
                           VendedorRepository vendedorRepository) {
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.vendedorRepository = vendedorRepository;
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
            return mapToResponse(existente.get());
        }

        Conversacion conversacion = new Conversacion();
        conversacion.setComprador(comprador);
        conversacion.setVendedor(vendedor);
        conversacion.setActiva(true);

        Conversacion guardada = conversacionRepository.save(conversacion);
        return mapToResponse(guardada);
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

        return conversaciones.stream().map(this::mapToResponse).collect(Collectors.toList());
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
        
        return mapToResponse(guardado);
    }

    private ConversacionResponse mapToResponse(Conversacion c) {
        return new ConversacionResponse(
                c.getId(),
                c.getFechaCreacion(),
                c.getActiva(),
                c.getComprador().getCorreo(),
                c.getVendedor().getId(),
                c.getVendedor().getNombreTienda()
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
