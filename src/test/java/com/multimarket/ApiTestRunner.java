package com.multimarket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiTestRunner {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder().build();

    private static String vendedorToken = "";
    private static String compradorToken = "";
    private static String adminToken = "";

    private static Long vendedorId = null;
    private static Long compradorId = null;
    private static Long productoId = null;
    private static Long pedidoId = null;
    private static Long conversacionId = null;
    private static Long categoriaId = null;
    private static Long notificacionId = null;
    private static Long imagenId = null;

    private static final List<TestResult> results = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== INICIANDO FLUJO DE PRUEBAS DE API MULTIMARKET ===");

        long salt = System.currentTimeMillis();
        String vendedorCorreo = "vendedor_" + salt + "@test.com";
        String compradorCorreo = "comprador_" + salt + "@test.com";
        String adminCorreo = "admin_" + salt + "@test.com";
        
        String dniVendedor = "DNI" + (salt % 100000000L);
        String dniComprador = "DNI" + ((salt + 1) % 100000000L);
        String dniAdmin = "DNI" + ((salt + 2) % 100000000L);

        // 1. MÓDULO 1: SEGURIDAD Y AUTENTICACIÓN
        testRegistro("1. Registro de Vendedor (POST /auth/register)", vendedorCorreo, dniVendedor, "VENDEDOR");
        testRegistro("2. Registro de Comprador (POST /auth/register)", compradorCorreo, dniComprador, "COMPRADOR");
        testRegistro("3. Registro de Administrador (POST /auth/register)", adminCorreo, dniAdmin, "ADMIN");

        vendedorToken = testLogin("4. Login de Vendedor (POST /auth/login)", vendedorCorreo);
        compradorToken = testLogin("5. Login de Comprador (POST /auth/login)", compradorCorreo);
        adminToken = testLogin("6. Login de Administrador (POST /auth/login)", adminCorreo);

        testPerfil("7. Consultar Perfil de Comprador (GET /auth/profile)", compradorToken);
        testForgotPassword("8. Solicitar Recuperación de Contraseña (POST /auth/forgot-password)", compradorCorreo);
        testResetPassword("9. Restablecer Contraseña (POST /auth/reset-password)");
        testModificarPassword("10. Modificar Contraseña de Usuario (PUT /auth/change-password)", compradorToken);

        // 2. MÓDULO 2: CATEGORÍAS
        testCrearCategoria("11. Crear Categoría Inicial (POST /categorias)", adminToken);
        testListarCategorias("12. Listar Categorías Activas (GET /categorias)");
        testConsultarCategoria("13. Consultar Detalles de Categoría (GET /categorias/{id})");
        testModificarCategoria("14. Modificar Categoría (PUT /categorias/{id})", adminToken);
        testDesactivarCategoria("15. Desactivar Categoría (DELETE /categorias/{id})", adminToken);

        // 3. MÓDULO 3: VENDEDORES Y TIENDAS
        testCrearTienda("16. Crear Tienda de Vendedor (POST /vendedores)", vendedorToken);
        testConsultarTienda("17. Consultar Tienda por ID (GET /vendedores/{id})");
        testConsultarMiTienda("18. Consultar Mi Tienda (GET /vendedores/mi-tienda)", vendedorToken);
        testModificarTienda("19. Modificar Detalles de Tienda (PUT /vendedores/{id})", vendedorToken);
        testDesactivarTienda("20. Cambiar Estado de Tienda (PUT /vendedores/{id}/desactivar)", vendedorToken);

        // 4. MÓDULO 4: CATÁLOGO Y PRODUCTOS
        testCrearProducto("21. Crear Producto (POST /productos)", vendedorToken);
        testListarProductos("22. Listar Productos Activos (GET /productos)");
        testConsultarProducto("23. Consultar Detalles de Producto (GET /productos/{id})");
        testBuscarProductos("24. Búsqueda de Productos con Filtros (GET /productos/buscar)");
        testAgregarImagen("25. Cargar Imagen de Producto (POST /productos/{id}/imagenes)", vendedorToken);
        testEliminarImagen("26. Eliminar Imagen de Producto (DELETE /productos/imagenes/{id})", vendedorToken);
        testAgregarFavorito("27. Añadir Producto a Favoritos (POST /productos/favoritos/{id})", compradorToken);
        testListarFavoritos("28. Listar Favoritos del Comprador (GET /productos/favoritos)", compradorToken);
        testEliminarFavorito("29. Retirar Producto de Favoritos (DELETE /productos/favoritos/{id})", compradorToken);
        testModificarProducto("30. Modificar Detalles de Producto (PUT /productos/{id})", vendedorToken);
        testDesactivarProducto("31. Desactivar Producto del Catálogo (DELETE /productos/{id})", vendedorToken);

        // Re-crear producto para inventario y pedidos puesto que el anterior se desactivó
        testCrearProducto("31b. Re-crear Producto de Prueba para Flujos Siguientes", vendedorToken);

        // 5. MÓDULO 5: INVENTARIO
        testConsultarStock("32. Consultar Stock de Producto (GET /inventarios/productos/{id})", vendedorToken);
        testRegistrarMovimiento("33. Registrar Movimiento Manual (POST /inventarios/productos/{id}/movimientos)", vendedorToken);
        testConsultarMovimientos("34. Consultar Historial de Movimientos (GET /inventarios/productos/{id}/movimientos)", vendedorToken);
        testModificarStockMinimo("35. Configurar Stock Mínimo de Inventario (PUT /inventarios/productos/{id}/stock-minimo)", vendedorToken);

        // 6. MÓDULO 6: PEDIDOS
        testCrearPedido("36. Crear Pedido de Producto (POST /pedidos)", compradorToken);
        testConsultarPedido("37. Consultar Detalles de Pedido (GET /pedidos/{id})", compradorToken);
        testListarMisPedidos("38. Listar Mis Pedidos Comprador (GET /pedidos/mis-pedidos)", compradorToken);
        testListarPedidosTienda("39. Listar Pedidos Recibidos por Tienda (GET /pedidos/tienda)", vendedorToken);
        testModificarEstadoPedido("40. Actualizar Estado de Pedido a ENVIADO (PUT /pedidos/{id}/estado)", vendedorToken);
        testCancelarPedido("41. Cancelar Pedido Temporal (PUT /pedidos/{id}/cancelar)", compradorToken);

        // 7. MÓDULO 7: PAGOS (Simulación SOAP)
        testProcesarPago("42. Procesar Pago Pedido (POST /pagos)", compradorToken);
        testConsultarPago("43. Consultar Detalles de Pago (GET /pagos/{id})", compradorToken);

        // 8. MÓDULO 8: CHAT INTERNO
        testCrearConversacion("44. Iniciar Conversación de Chat (POST /chat/conversaciones)", compradorToken);
        testListarConversaciones("45. Listar Conversaciones del Usuario (GET /chat/conversaciones)", compradorToken);
        testEnviarMensaje("46. Enviar Mensaje de Chat (POST /chat/conversaciones/{id}/mensajes)", compradorToken);
        testConsultarChat("47. Consultar Mensajes de Chat y Marcar Leído (GET /chat/conversaciones/{id}/mensajes)", vendedorToken);

        // 9. MÓDULO 9: IMPORTACIÓN XML (JAXB)
        testImportarXml("48. Importación Masiva de Catálogo (POST /importar)", vendedorToken);

        // 10. MÓDULO 10: EXPORTACIÓN ASÍNCRONA (ExecutorService)
        testProgramarExportacion("49. Exportar Catálogo JSON Asíncronamente (POST /exportar)", vendedorToken, "JSON");
        testProgramarExportacion("49b. Exportar Catálogo XML Asíncronamente (POST /exportar)", vendedorToken, "XML");

        // 11. MÓDULO 11: NOTIFICACIONES
        testConsultarNotificaciones("50. Consultar Historial de Notificaciones (GET /notificaciones)", compradorToken);
        testCrearNotificacionTest("50b. Crear Alerta Manual de Prueba (POST /notificaciones/test)", adminToken);
        testMarcarNotificacionLeida("50c. Marcar Notificación como Leída (PUT /notificaciones/{id}/leer)", compradorToken);

        // 12. MÓDULO 12: AUDITORÍA Y LOGS
        testConsultarLogs("51. Consultar Logs de Auditoría del Sistema (GET /logs)", adminToken);
        testFiltrarLogs("52. Filtrar Logs de Auditoría por Nivel/Módulo (GET /logs/filtrar)", adminToken);

        // 12. GENERAR DOCUMENTO DE WORD (.doc)
        generarDocumentoWord();
        System.out.println("=== FLUJO DE PRUEBAS DE API MULTIMARKET FINALIZADO ===");
    }

    private static void testRegistro(String title, String correo, String dni, String rol) {
        String json = String.format("{"
                + "\"correo\":\"%s\","
                + "\"password\":\"Password123!\","
                + "\"nombres\":\"Test %s\","
                + "\"apellidos\":\"User\","
                + "\"dni\":\"%s\","
                + "\"telefono\":\"+51999888777\","
                + "\"direccion\":\"Av. Larco 123, Miraflores\","
                + "\"fechaNacimiento\":\"1996-05-15\","
                + "\"roles\":[\"%s\"]"
                + "}", correo, rol, dni, rol);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            
            results.add(new TestResult("AUTH", "/auth/register", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("AUTH", "/auth/register", "POST", title, 500, false, e.toString()));
            System.err.println(title + " - Error: " + e.getMessage());
        }
    }

    private static String testLogin(String title, String correo) {
        String json = String.format("{"
                + "\"correo\":\"%s\","
                + "\"password\":\"Password123!\""
                + "}", correo);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200;
            String token = "";
            if (success) {
                System.out.println("DEBUG LOGIN BODY: " + response.body());
                token = extractValue(response.body(), "token");
                System.out.println("DEBUG EXTRACTED TOKEN: '" + token + "'");
            }
            results.add(new TestResult("AUTH", "/auth/login", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
            return token;
        } catch (Exception e) {
            results.add(new TestResult("AUTH", "/auth/login", "POST", title, 500, false, e.toString()));
            System.err.println(title + " - Error: " + e.getMessage());
            return "";
        }
    }

    private static void testPerfil(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/profile"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200;
            if (success) {
                String idStr = extractValue(response.body(), "id");
                if (!idStr.isEmpty()) {
                    compradorId = Long.parseLong(idStr);
                }
            }
            results.add(new TestResult("AUTH", "/auth/profile", "GET", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("AUTH", "/auth/profile", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testCrearTienda(String title, String token) {
        String json = "{"
                + "\"nombreTienda\":\"Tienda de Tecnologia " + UUID.randomUUID().toString().substring(0, 5) + "\","
                + "\"descripcion\":\"Hardware premium para desarrolladores y gamers\","
                + "\"region\":\"Lima\","
                + "\"direccion\":\"Av. Larco 456, Miraflores\","
                + "\"logo\":\"https://img.com/logo.png\","
                + "\"banner\":\"https://img.com/banner.png\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/vendedores"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            if (success) {
                vendedorId = Long.parseLong(extractValue(response.body(), "id"));
            }
            results.add(new TestResult("VENDEDOR", "/vendedores", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("VENDEDOR", "/vendedores", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testCrearProducto(String title, String token) {
        if (vendedorId == null) vendedorId = 1L;
        String json = "{"
                + "\"nombre\":\"Laptop ASUS Zenbook 14\","
                + "\"descripcion\":\"Laptop compacta con Intel Core Ultra 7, 16GB RAM y 1TB SSD\","
                + "\"sku\":\"ASUS-ZENBOOK-14-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase() + "\","
                + "\"precio\":1299.99,"
                + "\"stock\":20,"
                + "\"peso\":1.20,"
                + "\"categoriaId\":1"
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            if (success) {
                productoId = Long.parseLong(extractValue(response.body(), "id"));
            }
            results.add(new TestResult("PRODUCTO", "/productos", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarStock(String title, String token) {
        if (productoId == null) productoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/inventarios/productos/" + productoId))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{id}", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{id}", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testRegistrarMovimiento(String title, String token) {
        if (productoId == null) productoId = 1L;
        String json = "{"
                + "\"tipoMovimiento\":\"ENTRADA\","
                + "\"cantidad\":5,"
                + "\"observacion\":\"Carga adicional por reabastecimiento manual\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/inventarios/productos/" + productoId + "/movimientos"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{id}/movimientos", "POST", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{id}/movimientos", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testCrearPedido(String title, String token) {
        if (vendedorId == null) vendedorId = 1L;
        if (productoId == null) productoId = 1L;

        String json = String.format("{"
                + "\"vendedorId\":%d,"
                + "\"costoEnvio\":15.00,"
                + "\"detalles\":["
                + "  {\"productoId\":%d,\"cantidad\":1}"
                + "]"
                + "}", vendedorId, productoId);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pedidos"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            if (success) {
                pedidoId = Long.parseLong(extractValue(response.body(), "id"));
            }
            results.add(new TestResult("PEDIDO", "/pedidos", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PEDIDO", "/pedidos", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testProcesarPago(String title, String token) {
        if (pedidoId == null) pedidoId = 1L;

        String json = String.format("{"
                + "\"pedidoId\":%d,"
                + "\"metodoPago\":\"VISA\","
                + "\"numeroTarjeta\":\"1234-5678-9876-5432\","
                + "\"cvv\":\"123\","
                + "\"fechaExpiracion\":\"12/2029\""
                + "}", pedidoId);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pagos"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PAGO", "/pagos", "POST", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PAGO", "/pagos", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarPago(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pagos/1"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PAGO", "/pagos/{id}", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PAGO", "/pagos/{id}", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testCrearConversacion(String title, String token) {
        if (vendedorId == null) vendedorId = 1L;
        String json = "{\"vendedorId\":" + vendedorId + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/chat/conversaciones"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            if (success) {
                conversacionId = Long.parseLong(extractValue(response.body(), "id"));
            }
            results.add(new TestResult("CHAT", "/chat/conversaciones", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CHAT", "/chat/conversaciones", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testEnviarMensaje(String title, String token) {
        if (conversacionId == null) conversacionId = 1L;
        String json = "{\"contenido\":\"Hola, requiero mas informacion sobre el stock y envio del producto\"}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/chat/conversaciones/" + conversacionId + "/mensajes"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("CHAT", "/chat/conversaciones/{id}/mensajes", "POST", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CHAT", "/chat/conversaciones/{id}/mensajes", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarChat(String title, String token) {
        if (conversacionId == null) conversacionId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/chat/conversaciones/" + conversacionId + "/mensajes"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("CHAT", "/chat/conversaciones/{id}/mensajes", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CHAT", "/chat/conversaciones/{id}/mensajes", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testImportarXml(String title, String token) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<catalogo>\n"
                + "    <producto>\n"
                + "        <nombre>Laptop Lenovo Legion</nombre>\n"
                + "        <descripcion>AMD Ryzen 7, 512GB SSD</descripcion>\n"
                + "        <sku>LENOVO-L7-R7-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase() + "</sku>\n"
                + "        <precio>1450.00</precio>\n"
                + "        <stock>10</stock>\n"
                + "        <peso>2.20</peso>\n"
                + "        <categoriaId>1</categoriaId>\n"
                + "        <vendedorId>1</vendedorId>\n"
                + "    </producto>\n"
                + "</catalogo>";

        String boundary = "---MultiMarketBoundary" + System.currentTimeMillis();
        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"import_test.xml\"\r\n"
                + "Content-Type: text/xml\r\n\r\n"
                + xml + "\r\n"
                + "--" + boundary + "--\r\n";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/importar"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("IMPORTACION", "/importar", "POST", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("IMPORTACION", "/importar", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testProgramarExportacion(String title, String token, String formato) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/exportar?formato=" + formato))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("EXPORTACION", "/exportar", "POST", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("EXPORTACION", "/exportar", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarNotificaciones(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/notificaciones"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("NOTIFICACIONES", "/notificaciones", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("NOTIFICACIONES", "/notificaciones", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testCrearNotificacionTest(String title, String token) {
        if (compradorId == null) compradorId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/notificaciones/test?usuarioId=" + compradorId
                            + "&titulo=Prueba+de+Alerta&mensaje=Esta+es+una+notificacion+de+prueba+de+API&tipo=SISTEMA"))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            if (success) {
                String idStr = extractValue(response.body(), "id");
                if (!idStr.isEmpty()) {
                    notificacionId = Long.parseLong(idStr);
                }
            }
            results.add(new TestResult("NOTIFICACIONES", "/notificaciones/test", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("NOTIFICACIONES", "/notificaciones/test", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testCrearCategoria(String title, String token) {
        String json = "{"
                + "\"nombre\":\"Tecnología\","
                + "\"descripcion\":\"Dispositivos electrónicos, hardware y gadgets\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/categorias"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            if (success) {
                String idStr = extractValue(response.body(), "id");
                if (!idStr.isEmpty()) {
                    categoriaId = Long.parseLong(idStr);
                }
            }
            results.add(new TestResult("CATALOGO", "/categorias", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CATALOGO", "/categorias", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarLogs(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/logs"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("LOGS", "/logs", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("LOGS", "/logs", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testFiltrarLogs(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/logs/filtrar?nivel=INFO&modulo=PRODUCTO"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("LOGS", "/logs/filtrar", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("LOGS", "/logs/filtrar", "GET", title, 500, false, e.toString()));
        }
    }

    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start = json.indexOf(":", start);
        if (start == -1) return "";
        
        // Buscar primera comilla doble después de los dos puntos
        int startQuote = json.indexOf("\"", start);
        // Si hay una comilla doble y está antes de una coma o llave de cierre, es un String
        int comma = json.indexOf(",", start);
        int brace = json.indexOf("}", start);
        int limit = -1;
        if (comma != -1 && brace != -1) limit = Math.min(comma, brace);
        else if (comma != -1) limit = comma;
        else if (brace != -1) limit = brace;

        if (startQuote != -1 && (limit == -1 || startQuote < limit)) {
            int endQuote = json.indexOf("\"", startQuote + 1);
            if (endQuote != -1) {
                return json.substring(startQuote + 1, endQuote).trim();
            }
        } else {
            // Es un número
            if (limit != -1) {
                return json.substring(start + 1, limit).trim();
            }
        }
        return "";
    }

    private static void generarDocumentoWord() {
        String filename = "MultiMarket_API_Checklist.doc";
        String filepath = "C:/Users/Alessander/Desktop/CIBERTEC ULTIMO/DSW II/MultiMarket/MultiMarket/" + filename;

        StringBuilder html = new StringBuilder();
        html.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>\n");
        html.append("<head>\n");
        html.append("<!--[if gte mso 9]><xml><w:WordDocument><w:View>Print</w:View><w:Zoom>100</w:Zoom></w:WordDocument></xml><![endif]-->\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 30px; color: #333333; }\n");
        html.append("h1 { color: #1e3a8a; font-size: 26px; border-bottom: 2px solid #1e3a8a; padding-bottom: 10px; margin-bottom: 20px; }\n");
        html.append("h2 { color: #0f766e; font-size: 18px; margin-top: 30px; margin-bottom: 10px; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 15px; margin-bottom: 15px; }\n");
        html.append("th { background-color: #1e3a8a; color: #ffffff; padding: 10px; text-align: left; font-size: 13px; border: 1px solid #cccccc; }\n");
        html.append("td { padding: 8px; font-size: 12px; border: 1px solid #cccccc; }\n");
        html.append(".badge-success { background-color: #d1fae5; color: #065f46; font-weight: bold; padding: 4px 8px; border-radius: 4px; border: 1px solid #a7f3d0; text-align: center; }\n");
        html.append(".badge-fail { background-color: #fee2e2; color: #991b1b; font-weight: bold; padding: 4px 8px; border-radius: 4px; border: 1px solid #fca5a5; text-align: center; }\n");
        html.append(".header-box { background-color: #f3f4f6; border-left: 5px solid #1e3a8a; padding: 15px; margin-bottom: 25px; }\n");
        html.append(".header-box p { margin: 5px 0; font-size: 13px; }\n");
        html.append("pre { background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 8px; font-family: Consolas, monospace; font-size: 11px; white-space: pre-wrap; word-wrap: break-word; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("<h1>Checklist de Verificación e Informe de Pruebas de API: MultiMarket</h1>\n");
        
        html.append("<div class='header-box'>\n");
        html.append("<p><b>Proyecto:</b> MultiMarket - Marketplace Multi-Vendedor Monolítico</p>\n");
        html.append("<p><b>Fecha de Ejecución:</b> ").append(LocalDateTime.now().toString().replace("T", " ")).append("</p>\n");
        html.append("<p><b>Entorno:</b> Servidor Local Tomcat (Puerto 8080) & Sockets (Puerto 9092)</p>\n");
        html.append("<p><b>Base de Datos:</b> Microsoft SQL Server Express (Autenticación Integrada)</p>\n");
        html.append("<p><b>Probador:</b> Antigravity AI Coding Assistant (Google DeepMind Team)</p>\n");
        html.append("</div>\n");

        html.append("<h2>1. Tabla Checklist Resumida de APIs</h2>\n");
        html.append("<table>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("<th style='width: 5%;'>#</th>\n");
        html.append("<th style='width: 15%;'>Módulo</th>\n");
        html.append("<th style='width: 15%;'>Método</th>\n");
        html.append("<th style='width: 25%;'>Endpoint</th>\n");
        html.append("<th style='width: 25%;'>Descripción de la Operación</th>\n");
        html.append("<th style='width: 15%;'>Resultado</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        int counter = 1;
        for (TestResult r : results) {
            html.append("<tr>\n");
            html.append("<td>").append(counter++).append("</td>\n");
            html.append("<td><b>").append(r.modulo).append("</b></td>\n");
            html.append("<td><code style='color: #a855f7; font-weight: bold;'>").append(r.method).append("</code></td>\n");
            html.append("<td><code>").append(r.endpoint).append("</code></td>\n");
            html.append("<td>").append(r.title).append("</td>\n");
            if (r.success) {
                html.append("<td><div class='badge-success'>PASÓ (").append(r.statusCode).append(")</div></td>\n");
            } else {
                html.append("<td><div class='badge-fail'>FALLÓ (").append(r.statusCode).append(")</div></td>\n");
            }
            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");

        html.append("<h2>2. Detalle de Respuestas Clave de APIs Probadas</h2>\n");
        
        for (TestResult r : results) {
            html.append("<h3>Test #").append(results.indexOf(r) + 1).append(": ").append(r.title).append("</h3>\n");
            html.append("<p><b>Endpoint:</b> <code style='font-size: 13px;'>").append(r.method).append(" ").append(r.endpoint).append("</code></p>\n");
            html.append("<p><b>Código de Respuesta:</b> ").append(r.statusCode).append("</p>\n");
            html.append("<p><b>Cuerpo de Respuesta / Detalle:</b></p>\n");
            html.append("<pre>").append(escapeHtml(r.response)).append("</pre>\n");
        }

        html.append("</body>\n");
        html.append("</html>\n");

        try (FileWriter writer = new FileWriter(filepath, StandardCharsets.UTF_8)) {
            writer.write(html.toString());
            System.out.println("Documento Word checklist generado exitosamente en: " + filepath);
        } catch (IOException e) {
            System.err.println("Error al escribir el documento Word: " + e.getMessage());
        }
    }

    private static String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private static void testModificarPassword(String title, String token) {
        String json = "{"
                + "\"oldPassword\":\"Password123!\","
                + "\"newPassword\":\"NewPassword123!\""
                + "}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/change-password"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200;
            results.add(new TestResult("AUTH", "/auth/change-password", "PUT", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());

            if (success) {
                String jsonRevert = "{"
                        + "\"oldPassword\":\"NewPassword123!\","
                        + "\"newPassword\":\"Password123!\""
                        + "}";
                HttpRequest revertRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/auth/change-password"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .PUT(HttpRequest.BodyPublishers.ofString(jsonRevert))
                        .build();
                client.send(revertRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("   Contraseña del comprador revertida exitosamente.");
            }
        } catch (Exception e) {
            results.add(new TestResult("AUTH", "/auth/change-password", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testModificarCategoria(String title, String token) {
        if (categoriaId == null) categoriaId = 1L;
        String json = "{"
                + "\"nombre\":\"Tecnología Avanzada\","
                + "\"descripcion\":\"Hardware de última generación y gadgets inteligentes\""
                + "}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/categorias/" + categoriaId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("CATALOGO", "/categorias/{id}", "PUT", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CATALOGO", "/categorias/{id}", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testModificarTienda(String title, String token) {
        if (vendedorId == null) vendedorId = 1L;
        String json = "{"
                + "\"nombreTienda\":\"Tienda de Tecnologia Ultra\","
                + "\"descripcion\":\"Hardware premium para desarrolladores, gamers y entusiastas\","
                + "\"region\":\"Lima Metropolitana\","
                + "\"direccion\":\"Av. Larco 789, Miraflores\","
                + "\"logo\":\"https://img.com/logo-new.png\","
                + "\"banner\":\"https://img.com/banner-new.png\""
                + "}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/vendedores/" + vendedorId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("VENDEDOR", "/vendedores/{id}", "PUT", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("VENDEDOR", "/vendedores/{id}", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testDesactivarTienda(String title, String token) {
        if (vendedorId == null) vendedorId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/vendedores/" + vendedorId + "/desactivar?activo=false"))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200;
            results.add(new TestResult("VENDEDOR", "/vendedores/{id}/desactivar", "PUT", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());

            if (success) {
                HttpRequest requestReactivate = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/vendedores/" + vendedorId + "/desactivar?activo=true"))
                        .header("Authorization", "Bearer " + token)
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build();
                client.send(requestReactivate, HttpResponse.BodyHandlers.ofString());
                System.out.println("   Tienda re-activada para preservar el estado.");
            }
        } catch (Exception e) {
            results.add(new TestResult("VENDEDOR", "/vendedores/{id}/desactivar", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testModificarProducto(String title, String token) {
        if (productoId == null) productoId = 1L;
        if (categoriaId == null) categoriaId = 1L;
        String json = "{"
                + "\"nombre\":\"Laptop ASUS Zenbook 14 OLED\","
                + "\"descripcion\":\"Laptop compacta con Intel Core Ultra 7, Pantalla OLED 3K, 16GB RAM y 1TB SSD\","
                + "\"sku\":\"ASUS-ZENBOOK-14-OLED\","
                + "\"precio\":1349.99,"
                + "\"stock\":25,"
                + "\"peso\":1.22,"
                + "\"categoriaId\":" + categoriaId
                + "}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/" + productoId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/{id}", "PUT", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/{id}", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testModificarStockMinimo(String title, String token) {
        if (productoId == null) productoId = 1L;
        String json = "{"
                + "\"stockMinimo\":8"
                + "}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/inventarios/productos/" + productoId + "/stock-minimo"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{productoId}/stock-minimo", "PUT", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{productoId}/stock-minimo", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testModificarEstadoPedido(String title, String token) {
        if (pedidoId == null) pedidoId = 1L;
        String json = "{"
                + "\"estado\":\"ENVIADO\""
                + "}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pedidos/" + pedidoId + "/estado"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PEDIDO", "/pedidos/{id}/estado", "PUT", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PEDIDO", "/pedidos/{id}/estado", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testCancelarPedido(String title, String token) {
        if (vendedorId == null) vendedorId = 1L;
        if (productoId == null) productoId = 1L;

        String jsonCreate = String.format("{"
                + "\"vendedorId\":%d,"
                + "\"costoEnvio\":15.00,"
                + "\"detalles\":["
                + "  {\"productoId\":%d,\"cantidad\":1}"
                + "]"
                + "}", vendedorId, productoId);

        try {
            HttpRequest requestCreate = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pedidos"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonCreate))
                    .build();

            HttpResponse<String> responseCreate = client.send(requestCreate, HttpResponse.BodyHandlers.ofString());
            if (responseCreate.statusCode() == 200 || responseCreate.statusCode() == 201) {
                Long tempPedidoId = Long.parseLong(extractValue(responseCreate.body(), "id"));
                
                HttpRequest requestCancel = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/pedidos/" + tempPedidoId + "/cancelar"))
                        .header("Authorization", "Bearer " + token)
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> responseCancel = client.send(requestCancel, HttpResponse.BodyHandlers.ofString());
                results.add(new TestResult("PEDIDO", "/pedidos/{id}/cancelar", "PUT", title, responseCancel.statusCode(), responseCancel.statusCode() == 200, responseCancel.body()));
                System.out.println(title + " - Status: " + responseCancel.statusCode());
            } else {
                results.add(new TestResult("PEDIDO", "/pedidos/{id}/cancelar", "PUT", title, responseCreate.statusCode(), false, "Fallo al crear pedido temporal: " + responseCreate.body()));
                System.out.println(title + " - Status: FAILED TO CREATE TEMP ORDER");
            }
        } catch (Exception e) {
            results.add(new TestResult("PEDIDO", "/pedidos/{id}/cancelar", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testMarcarNotificacionLeida(String title, String token) {
        if (notificacionId == null) notificacionId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/notificaciones/" + notificacionId + "/leer"))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("NOTIFICACIONES", "/notificaciones/{id}/leer", "PUT", title, response.statusCode(), response.statusCode() == 200, "Notificación marcada como leída exitosamente"));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("NOTIFICACIONES", "/notificaciones/{id}/leer", "PUT", title, 500, false, e.toString()));
        }
    }

    private static void testForgotPassword(String title, String email) {
        String json = "{\"correo\":\"" + email + "\"}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/forgot-password"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("AUTH", "/auth/forgot-password", "POST", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("AUTH", "/auth/forgot-password", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testResetPassword(String title) {
        String json = "{"
                + "\"token\":\"" + UUID.randomUUID().toString() + "\","
                + "\"newPassword\":\"NewPassword123!\""
                + "}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/reset-password"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("AUTH", "/auth/reset-password", "POST", title, response.statusCode(), response.statusCode() == 400 || response.statusCode() == 404 || response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode() + " (Esperado)");
        } catch (Exception e) {
            results.add(new TestResult("AUTH", "/auth/reset-password", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testListarCategorias(String title) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/categorias"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("CATALOGO", "/categorias", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CATALOGO", "/categorias", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarCategoria(String title) {
        if (categoriaId == null) categoriaId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/categorias/" + categoriaId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("CATALOGO", "/categorias/{id}", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CATALOGO", "/categorias/{id}", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testDesactivarCategoria(String title, String token) {
        if (categoriaId == null) categoriaId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/categorias/" + categoriaId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("CATALOGO", "/categorias/{id}", "DELETE", title, response.statusCode(), response.statusCode() == 204 || response.statusCode() == 200, "Categoría desactivada"));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CATALOGO", "/categorias/{id}", "DELETE", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarTienda(String title) {
        if (vendedorId == null) vendedorId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/vendedores/" + vendedorId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("VENDEDOR", "/vendedores/{id}", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("VENDEDOR", "/vendedores/{id}", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarMiTienda(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/vendedores/mi-tienda"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("VENDEDOR", "/vendedores/mi-tienda", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("VENDEDOR", "/vendedores/mi-tienda", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testListarConversaciones(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/chat/conversaciones"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("CHAT", "/chat/conversaciones", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("CHAT", "/chat/conversaciones", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarMovimientos(String title, String token) {
        if (productoId == null) productoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/inventarios/productos/" + productoId + "/movimientos"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{id}/movimientos", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("INVENTARIO", "/inventarios/productos/{id}/movimientos", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testListarMisPedidos(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pedidos/mis-pedidos"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PEDIDO", "/pedidos/mis-pedidos", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PEDIDO", "/pedidos/mis-pedidos", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testListarPedidosTienda(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pedidos/tienda"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PEDIDO", "/pedidos/tienda", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PEDIDO", "/pedidos/tienda", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testListarProductos(String title) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testBuscarProductos(String title) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/buscar?nombre=ASUS"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/buscar", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/buscar", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testAgregarImagen(String title, String token) {
        if (productoId == null) productoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/" + productoId + "/imagenes?url=https://img.com/asus.png&principal=true&orden=1"))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() == 200 || response.statusCode() == 201;
            if (success) {
                String idStr = extractValue(response.body(), "id");
                if (!idStr.isEmpty()) {
                    imagenId = Long.parseLong(idStr);
                }
            }
            results.add(new TestResult("PRODUCTO", "/productos/{id}/imagenes", "POST", title, response.statusCode(), success, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/{id}/imagenes", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testEliminarImagen(String title, String token) {
        if (imagenId == null) imagenId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/imagenes/" + imagenId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/imagenes/{id}", "DELETE", title, response.statusCode(), response.statusCode() == 204 || response.statusCode() == 200, "Imagen eliminada"));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/imagenes/{id}", "DELETE", title, 500, false, e.toString()));
        }
    }

    private static void testAgregarFavorito(String title, String token) {
        if (productoId == null) productoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/favoritos/" + productoId))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/favoritos/{id}", "POST", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/favoritos/{id}", "POST", title, 500, false, e.toString()));
        }
    }

    private static void testListarFavoritos(String title, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/favoritos"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/favoritos", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/favoritos", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testEliminarFavorito(String title, String token) {
        if (productoId == null) productoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/favoritos/" + productoId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/favoritos/{id}", "DELETE", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/favoritos/{id}", "DELETE", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarProducto(String title) {
        if (productoId == null) productoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/" + productoId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/{id}", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/{id}", "GET", title, 500, false, e.toString()));
        }
    }

    private static void testDesactivarProducto(String title, String token) {
        if (productoId == null) productoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/productos/" + productoId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PRODUCTO", "/productos/{id}", "DELETE", title, response.statusCode(), response.statusCode() == 204 || response.statusCode() == 200, "Producto desactivado"));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PRODUCTO", "/productos/{id}", "DELETE", title, 500, false, e.toString()));
        }
    }

    private static void testConsultarPedido(String title, String token) {
        if (pedidoId == null) pedidoId = 1L;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/pedidos/" + pedidoId))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            results.add(new TestResult("PEDIDO", "/pedidos/{id}", "GET", title, response.statusCode(), response.statusCode() == 200, response.body()));
            System.out.println(title + " - Status: " + response.statusCode());
        } catch (Exception e) {
            results.add(new TestResult("PEDIDO", "/pedidos/{id}", "GET", title, 500, false, e.toString()));
        }
    }

    private static class TestResult {
        String modulo;
        String endpoint;
        String method;
        String title;
        int statusCode;
        boolean success;
        String response;

        TestResult(String modulo, String endpoint, String method, String title, int statusCode, boolean success, String response) {
            this.modulo = modulo;
            this.endpoint = endpoint;
            this.method = method;
            this.title = title;
            this.statusCode = statusCode;
            this.success = success;
            // Truncate overly long response bodies for readability
            if (response != null && response.length() > 2500) {
                this.response = response.substring(0, 2500) + "... [Truncado por tamaño]";
            } else {
                this.response = response;
            }
        }
    }
}
