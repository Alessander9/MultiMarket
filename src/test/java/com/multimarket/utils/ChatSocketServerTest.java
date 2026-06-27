package com.multimarket.utils;

import com.multimarket.dto.MensajeResponse;
import com.multimarket.models.Conversacion;
import com.multimarket.models.Usuario;
import com.multimarket.models.Vendedor;
import com.multimarket.repositories.ConversacionRepository;
import com.multimarket.services.Interfaces.ChatService;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatSocketServerTest {

    @Test
    void tcpServerShouldAcceptConnectAndSendCommands() throws Exception {
        assumeTrue(isPortAvailable(9092), "Puerto 9092 ocupado por otra instancia; se omite el test TCP aislado.");

        ChatService chatService = mock(ChatService.class);
        ConversacionRepository repository = mock(ConversacionRepository.class);

        Usuario buyer = new Usuario();
        buyer.setCorreo("buyer@test.com");
        Usuario sellerUser = new Usuario();
        sellerUser.setCorreo("seller@test.com");
        Vendedor vendedor = new Vendedor();
        vendedor.setUsuario(sellerUser);

        Conversacion conversacion = new Conversacion();
        conversacion.setId(100L);
        conversacion.setComprador(buyer);
        conversacion.setVendedor(vendedor);
        conversacion.setActiva(true);

        when(chatService.enviarMensaje(100L, "buyer@test.com", "Hola TCP"))
                .thenReturn(new MensajeResponse(7L, "Hola TCP", LocalDateTime.now(), false, "buyer@test.com"));
        when(repository.findById(100L)).thenReturn(Optional.of(conversacion));

        ChatSocketServer server = new ChatSocketServer(chatService, repository);
        server.run();

        Thread.sleep(500);

        try (Socket socket = new Socket("127.0.0.1", 9092);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println("CONNECT buyer@test.com");
            String connectAck = readLineWithRetry(reader, 20, 100);
            assertTrue(connectAck.contains("OK: Conectado"));

            writer.println("SEND 100 buyer@test.com Hola TCP");
            String sendAck = readLineWithRetry(reader, 20, 100);
            assertTrue(sendAck.contains("OK: Mensaje guardado"));
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String readLineWithRetry(BufferedReader reader, int attempts, long delayMs) throws Exception {
        for (int i = 0; i < attempts; i++) {
            if (reader.ready()) {
                String line = reader.readLine();
                if (line != null) {
                    return line;
                }
            }
            Thread.sleep(delayMs);
        }
        return reader.readLine();
    }
}
