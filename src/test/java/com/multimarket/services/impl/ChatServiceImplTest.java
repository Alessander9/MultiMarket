package com.multimarket.services.impl;

import com.multimarket.dto.ConversacionRequest;
import com.multimarket.models.Conversacion;
import com.multimarket.models.Mensaje;
import com.multimarket.models.Rol;
import com.multimarket.models.RolNombre;
import com.multimarket.models.Usuario;
import com.multimarket.models.Vendedor;
import com.multimarket.repositories.ConversacionRepository;
import com.multimarket.repositories.MensajeRepository;
import com.multimarket.repositories.UsuarioRepository;
import com.multimarket.repositories.VendedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock private ConversacionRepository conversacionRepository;
    @Mock private MensajeRepository mensajeRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private VendedorRepository vendedorRepository;

    @InjectMocks
    private ChatServiceImpl service;

    private Usuario comprador;
    private Usuario vendedorUsuario;
    private Vendedor vendedor;

    @BeforeEach
    void setUp() {
        comprador = new Usuario();
        comprador.setId(1L);
        comprador.setCorreo("buyer@test.com");
        comprador.setRoles(Set.of(new Rol(RolNombre.COMPRADOR, "Comprador")));

        vendedorUsuario = new Usuario();
        vendedorUsuario.setId(2L);
        vendedorUsuario.setCorreo("seller@test.com");
        vendedorUsuario.setRoles(Set.of(new Rol(RolNombre.VENDEDOR, "Vendedor")));

        vendedor = new Vendedor();
        vendedor.setId(10L);
        vendedor.setUsuario(vendedorUsuario);
        vendedor.setNombreTienda("Tienda QA");
    }

    @Test
    void crearConversacionShouldReuseExistingConversation() {
        Conversacion existing = new Conversacion();
        existing.setId(99L);
        existing.setComprador(comprador);
        existing.setVendedor(vendedor);
        existing.setActiva(true);

        when(usuarioRepository.findByCorreo(comprador.getCorreo())).thenReturn(Optional.of(comprador));
        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(vendedor));
        when(conversacionRepository.findByCompradorIdAndVendedorId(1L, 10L)).thenReturn(Optional.of(existing));

        var response = service.crearConversacion(comprador.getCorreo(), buildConversationRequest(10L));

        assertEquals(99L, response.getId());
        assertEquals("buyer@test.com", response.getCompradorCorreo());
        assertEquals("Tienda QA", response.getVendedorTienda());
    }

    @Test
    void crearConversacionShouldRejectSelfChat() {
        vendedor.setUsuario(comprador);

        when(usuarioRepository.findByCorreo(comprador.getCorreo())).thenReturn(Optional.of(comprador));
        when(vendedorRepository.findById(10L)).thenReturn(Optional.of(vendedor));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearConversacion(comprador.getCorreo(), buildConversationRequest(10L)));

        assertTrue(ex.getMessage().contains("propia tienda"));
    }

    @Test
    void listarConversacionesPorUsuarioShouldUseSellerQueryWhenRoleIsSeller() {
        Conversacion conversation = new Conversacion();
        conversation.setId(5L);
        conversation.setComprador(comprador);
        conversation.setVendedor(vendedor);
        conversation.setActiva(true);

        when(usuarioRepository.findByCorreo(vendedorUsuario.getCorreo())).thenReturn(Optional.of(vendedorUsuario));
        when(conversacionRepository.findByVendedorUsuarioCorreoOrderByFechaCreacionDesc(vendedorUsuario.getCorreo()))
                .thenReturn(List.of(conversation));

        var result = service.listarConversacionesPorUsuario(vendedorUsuario.getCorreo());

        assertEquals(1, result.size());
        assertEquals("Tienda QA", result.get(0).getVendedorTienda());
    }

    @Test
    void obtenerHistorialShouldMarkIncomingMessagesAsRead() {
        Conversacion conversation = new Conversacion();
        conversation.setId(7L);
        conversation.setComprador(comprador);
        conversation.setVendedor(vendedor);
        conversation.setActiva(true);

        Mensaje incoming = new Mensaje();
        incoming.setId(1L);
        incoming.setContenido("Hola");
        incoming.setConversacion(conversation);
        incoming.setRemitente(vendedorUsuario);
        incoming.setLeido(false);

        when(conversacionRepository.findById(7L)).thenReturn(Optional.of(conversation));
        when(usuarioRepository.findByCorreo(comprador.getCorreo())).thenReturn(Optional.of(comprador));
        when(mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(7L)).thenReturn(List.of(incoming));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.obtenerHistorial(7L, comprador.getCorreo());

        assertEquals(1, result.size());
        assertTrue(incoming.getLeido());
        verify(mensajeRepository).save(incoming);
    }

    @Test
    void obtenerHistorialShouldRejectForeignUser() {
        Conversacion conversation = new Conversacion();
        conversation.setId(8L);
        conversation.setComprador(comprador);
        conversation.setVendedor(vendedor);
        conversation.setActiva(true);

        Usuario outsider = new Usuario();
        outsider.setId(77L);
        outsider.setCorreo("outsider@test.com");

        when(conversacionRepository.findById(8L)).thenReturn(Optional.of(conversation));
        when(usuarioRepository.findByCorreo(outsider.getCorreo())).thenReturn(Optional.of(outsider));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.obtenerHistorial(8L, outsider.getCorreo()));

        assertTrue(ex.getMessage().contains("Acceso Denegado"));
    }

    @Test
    void enviarMensajeShouldPersistContentForParticipant() {
        Conversacion conversation = new Conversacion();
        conversation.setId(11L);
        conversation.setComprador(comprador);
        conversation.setVendedor(vendedor);
        conversation.setActiva(true);

        when(conversacionRepository.findById(11L)).thenReturn(Optional.of(conversation));
        when(usuarioRepository.findByCorreo(vendedorUsuario.getCorreo())).thenReturn(Optional.of(vendedorUsuario));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> {
            Mensaje message = inv.getArgument(0);
            message.setId(500L);
            return message;
        });

        var result = service.enviarMensaje(11L, vendedorUsuario.getCorreo(), "Mensaje QA");

        assertEquals(500L, result.getId());
        assertEquals("Mensaje QA", result.getContenido());
        assertEquals(vendedorUsuario.getCorreo(), result.getRemitenteCorreo());
    }

    @Test
    void enviarMensajeShouldRejectInactiveConversation() {
        Conversacion conversation = new Conversacion();
        conversation.setId(12L);
        conversation.setComprador(comprador);
        conversation.setVendedor(vendedor);
        conversation.setActiva(false);

        when(conversacionRepository.findById(12L)).thenReturn(Optional.of(conversation));
        when(usuarioRepository.findByCorreo(comprador.getCorreo())).thenReturn(Optional.of(comprador));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.enviarMensaje(12L, comprador.getCorreo(), "Hola"));

        assertTrue(ex.getMessage().contains("archivada"));
    }

    private ConversacionRequest buildConversationRequest(Long vendedorId) {
        ConversacionRequest request = new ConversacionRequest();
        request.setVendedorId(vendedorId);
        return request;
    }
}
