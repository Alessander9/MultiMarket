package com.multimarket.utils;

import com.multimarket.dto.MensajeResponse;
import com.multimarket.models.Conversacion;
import com.multimarket.repositories.ConversacionRepository;
import com.multimarket.services.Interfaces.ChatService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ChatSocketServer implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(ChatSocketServer.class.getName());
    private static final int PORT = 9092;

    private final ChatService chatService;
    private final ConversacionRepository conversacionRepository;

    // Mapa de conexiones activas indexado por el correo del usuario
    private final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public ChatSocketServer(ChatService chatService, ConversacionRepository conversacionRepository) {
        this.chatService = chatService;
        this.conversacionRepository = conversacionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Iniciamos el ServerSocket en un hilo independiente para no bloquear el arranque de Tomcat
        Thread serverThread = new Thread(this::startServer);
        serverThread.setDaemon(true);
        serverThread.setName("ChatSocketServer-MainThread");
        serverThread.start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.log(Level.INFO, "====== CHAT TCP SOCKET SERVER INICIADO EN EL PUERTO: " + PORT + " ======");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOGGER.log(Level.INFO, "Nueva conexión TCP entrante desde: " + clientSocket.getRemoteSocketAddress());
                
                // Spawnear un hilo para atender al cliente
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fallo crítico en el servidor de sockets de Chat: ", e);
        }
    }

    // Hilo manejador de cada socket de cliente
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String userEmail;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.writer = new PrintWriter(socket.getOutputStream(), true);

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    // Protocolo sencillo:
                    // 1. CONNECT <correo> -> Registra la conexión
                    // 2. SEND <conversacionId> <remitenteCorreo> <contenido...> -> Envía y persiste el mensaje
                    
                    if (line.startsWith("CONNECT ")) {
                        handleConnect(line.substring(8).trim());
                    } else if (line.startsWith("SEND ")) {
                        handleSend(line.substring(5).trim());
                    } else {
                        writer.println("ERROR: Comando desconocido. Comandos soportados: CONNECT <correo>, SEND <convId> <remitente> <mensaje>");
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error o desconexión en hilo de cliente (" + userEmail + "): " + e.getMessage());
            } finally {
                cleanUpConnection();
            }
        }

        private void handleConnect(String email) {
            if (email.isEmpty()) {
                writer.println("ERROR: Correo inválido");
                return;
            }
            this.userEmail = email;
            activeClients.put(email, this);
            LOGGER.log(Level.INFO, "Usuario '" + email + "' registrado en la sesión de Socket en tiempo real.");
            writer.println("OK: Conectado como " + email);
        }

        private void handleSend(String payload) {
            if (userEmail == null) {
                writer.println("ERROR: Primero debes autenticarte usando: CONNECT <correo>");
                return;
            }

            try {
                // Parsear: <conversacionId> <remitenteCorreo> <contenido...>
                String[] parts = payload.split(" ", 3);
                if (parts.length < 3) {
                    writer.println("ERROR: Formato incorrecto. Uso: SEND <conversacionId> <remitente> <contenido>");
                    return;
                }

                Long conversacionId = Long.parseLong(parts[0]);
                String remitente = parts[1];
                String contenido = parts[2];

                // Seguridad: Validar que el remitente coincida con el correo del socket
                if (!remitente.equals(userEmail)) {
                    writer.println("ERROR: No puedes enviar mensajes a nombre de otra persona.");
                    return;
                }

                // Persistir el mensaje usando el servicio de base de datos
                MensajeResponse savedMsg = chatService.enviarMensaje(conversacionId, remitente, contenido);
                writer.println("OK: Mensaje guardado con ID: " + savedMsg.getId());

                // Obtener los participantes de la conversación para enviarlo en tiempo real
                Conversacion conv = conversacionRepository.findById(conversacionId)
                        .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada"));

                // Determinar el destinatario
                String destinatarioCorreo = conv.getComprador().getCorreo().equals(remitente) 
                        ? conv.getVendedor().getUsuario().getCorreo() 
                        : conv.getComprador().getCorreo();

                // Si el destinatario está online, enviárselo directamente por Socket
                ClientHandler recipientHandler = activeClients.get(destinatarioCorreo);
                if (recipientHandler != null) {
                    recipientHandler.sendMessage("NEW_MESSAGE " + conversacionId + " " + remitente + " " + contenido);
                    LOGGER.log(Level.INFO, "Mensaje de '" + remitente + "' reenviado por Socket en tiempo real a '" + destinatarioCorreo + "'");
                }

            } catch (NumberFormatException e) {
                writer.println("ERROR: El ID de la conversación debe ser numérico.");
            } catch (Exception e) {
                writer.println("ERROR: No se pudo procesar el envío: " + e.getMessage());
            }
        }

        public void sendMessage(String message) {
            if (writer != null) {
                writer.println(message);
            }
        }

        private void cleanUpConnection() {
            if (userEmail != null) {
                activeClients.remove(userEmail);
                LOGGER.log(Level.INFO, "Usuario '" + userEmail + "' desconectado del servidor de Sockets.");
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error al cerrar socket: " + e.getMessage());
            }
        }
    }
}
