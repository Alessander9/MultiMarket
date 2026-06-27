package com.multimarket.utils;

import com.multimarket.models.*;
import com.multimarket.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoriaRepository categoriaRepository;
    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    public DataSeeder(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                      PasswordEncoder passwordEncoder, CategoriaRepository categoriaRepository,
                      VendedorRepository vendedorRepository, ProductoRepository productoRepository,
                      InventarioRepository inventarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoriaRepository = categoriaRepository;
        this.vendedorRepository = vendedorRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    // Café Products Pool
    private static final String[] CAFE_NAMES = {
        "Blend de la Casa", "Espresso Supremo", "Aroma de Altura", "Granos Selectos", "Tostado Oscuro",
        "Geisha Reserva Especial", "Bourbon Orgánico", "Caturra Selección", "Café Miel de Bosque", "Café del Huerto",
        "Finca La Flor", "Espresso Roast", "Cold Brew Blend", "Descafeinado Natural", "Grano Entero Gourmet",
        "Molido Fino Intenso", "Molido Medio Clásico", "Néctar Andino", "Reserva de Valle", "Café Pacamara"
    };
    private static final String[] CAFE_IMAGES = {
        "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=600",
        "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600",
        "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600",
        "https://images.unsplash.com/photo-1447933601403-0c6688de566e?w=600",
        "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=600",
        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600",
        "https://images.unsplash.com/photo-1511920170033-f8396924c348?w=600",
        "https://images.unsplash.com/photo-1507133750040-4a8f57021571?w=600",
        "https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=600",
        "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=600",
        "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=600",
        "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=600",
        "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600",
        "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600",
        "https://images.unsplash.com/photo-1447933601403-0c6688de566e?w=600",
        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600",
        "https://images.unsplash.com/photo-1511920170033-f8396924c348?w=600",
        "https://images.unsplash.com/photo-1507133750040-4a8f57021571?w=600",
        "https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=600",
        "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=600"
    };

    // Chocolate Products Pool
    private static final String[] CHOCO_NAMES = {
        "Chocolate Negro 70%", "Chocolate con Leche 45%", "Trufas de Cacao", "Chocolate con Maní", "Chocolate Blanco Cream",
        "Barra de Cacao Puro 100%", "Chocolate con Arándanos", "Bombones Surtidos", "Chocolate Caliente en Polvo", "Cobertura de Chocolate",
        "Cacao en Polvo Premium", "Barra de Cacao Fino 85%", "Chocolate con Sal de Mar", "Chocolate con Café", "Barra de Cacao Chuncho",
        "Chocolate con Almendras", "Trufas de Maracuyá", "Chocolate con Ají Limón", "Chocolate con Quinua Pop", "Barra Fondant Clásica"
    };
    private static final String[] CHOCO_IMAGES = {
        "https://images.unsplash.com/photo-1548907040-4d42b52125e0?w=600",
        "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=600",
        "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600",
        "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600",
        "https://images.unsplash.com/photo-1546450094-1a3b508ad00d?w=600",
        "https://images.unsplash.com/photo-1548907040-4d42b52125e0?w=600",
        "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=600",
        "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600",
        "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600",
        "https://images.unsplash.com/photo-1546450094-1a3b508ad00d?w=600",
        "https://images.unsplash.com/photo-1548907040-4d42b52125e0?w=600",
        "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=600",
        "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600",
        "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600",
        "https://images.unsplash.com/photo-1546450094-1a3b508ad00d?w=600",
        "https://images.unsplash.com/photo-1548907040-4d42b52125e0?w=600",
        "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=600",
        "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600",
        "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600",
        "https://images.unsplash.com/photo-1546450094-1a3b508ad00d?w=600"
    };

    // Artesanías Products Pool
    private static final String[] ART_NAMES = {
        "Torito de Pucará Grande", "Retablo Ayacuchano Mediano", "Vasija de Barro Pintada", "Mate Burilado Decorativo", "Plato de Cerámica Tradicional",
        "Llamita tallada en Piedra", "Espejo con Marco Dorado", "Máscara de la Diablada", "Cofre de Madera Tallado", "Florero de Cerámica Esmaltada",
        "Pulsera de Plata Filigrana", "Collar de Huayruro y Plata", "Torito de Pucará Mini", "Cuadro Andino en Relieve", "Adorno de Nacimiento Barro",
        "Portavelas Rústico", "Cesta de Paja Tejida", "Jarrón Decorativo Rústico", "Figura de Alpaca de Lana", "Aretes de Plata Catacaos"
    };
    private static final String[] ART_IMAGES = {
        "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1618220179428-22790b461013?w=600",
        "https://images.unsplash.com/photo-1576016770956-debb63d90029?w=600",
        "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1618220179428-22790b461013?w=600",
        "https://images.unsplash.com/photo-1576016770956-debb63d90029?w=600",
        "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1618220179428-22790b461013?w=600",
        "https://images.unsplash.com/photo-1576016770956-debb63d90029?w=600",
        "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1618220179428-22790b461013?w=600",
        "https://images.unsplash.com/photo-1576016770956-debb63d90029?w=600",
        "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1618220179428-22790b461013?w=600",
        "https://images.unsplash.com/photo-1576016770956-debb63d90029?w=600"
    };

    // Textiles Products Pool
    private static final String[] TEXT_NAMES = {
        "Chalina de Alpaca Fina", "Chompa de Alpaca Baby", "Manta Andina Multicolor", "Poncho de Lana de Oveja", "Chullo Tradicional Puno",
        "Alfombra de Telar Hecho a Mano", "Cojín Decorativo Bordado", "Bolso de Telar Étnico", "Guantes de Lana de Alpaca", "Chaqueta Étnica Andina",
        "Chal de Fina Alpaca", "Medias de Alpaca Térmicas", "Poncho Cusco Premium", "Gorro de Lana Gruesa", "Cojín de Alpaca Suave",
        "Carpeta de Telar Decorativa", "Camino de Mesa Andino", "Faja Tradicional Bordada", "Monedero de Telar con Cierre", "Chalina Baby Alpaca Negra"
    };
    private static final String[] TEXT_IMAGES = {
        "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600",
        "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=600",
        "https://images.unsplash.com/photo-1528740569068-3006f394586a?w=600",
        "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600",
        "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=600",
        "https://images.unsplash.com/photo-1528740569068-3006f394586a?w=600",
        "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600",
        "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=600",
        "https://images.unsplash.com/photo-1528740569068-3006f394586a?w=600",
        "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600",
        "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=600",
        "https://images.unsplash.com/photo-1528740569068-3006f394586a?w=600",
        "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600",
        "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=600",
        "https://images.unsplash.com/photo-1528740569068-3006f394586a?w=600",
        "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600",
        "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=600",
        "https://images.unsplash.com/photo-1528740569068-3006f394586a?w=600",
        "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600",
        "https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=600"
    };

    // Miel Products Pool
    private static final String[] MIEL_NAMES = {
        "Miel de Eucalipto 1kg", "Miel de Naranjo 500g", "Miel de Flores Silvestres 1kg", "Miel de Algarrobo 1kg", "Polen Organico Granulado",
        "Jalea Real Pura 50g", "Propóleo en Spray 30ml", "Miel con Kión y Limón", "Crema de Miel Untable", "Panal de Abeja en Frasco",
        "Miel de Romero 500g", "Miel Multifloral del Cusco", "Polen Nutritivo 250g", "Propóleo en Gotas", "Caramelos de Miel y Eucalipto",
        "Miel de Eucalipto 500g", "Miel con Propóleo Reforzado", "Miel de Café Orgánica", "Miel Silvestre del Norte 500g", "Extracto de Propóleo Concentrado"
    };
    private static final String[] MIEL_IMAGES = {
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600",
        "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600",
        "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600"
    };

    // Ferretería Products Pool
    private static final String[] FERR_NAMES = {
        "Taladro Percutor DeWalt 20V Max", "Juego de Herramientas Stanley 110p", "Amoladora Angular Bosch 850W", "Caja de Herramientas Plástica 20\"", "Cerradura Digital Inteligente Yale",
        "Set de Destornilladores Imantados", "Pintura Látex Premium CPP", "Candado de Acero Blindado Forte", "Rotomartillo SDS Plus Makita", "Martillo de Uña Tramontina",
        "Wincha Métrica Stanley 8m", "Linterna LED Recargable 1000Lm", "Juego de Llaves Mixtas 12p", "Sierra Circular Black+Decker", "Cable Eléctrico Indeco Nro 12",
        "Set de Brochas Atlas Premium", "Cinta Aisladora 3M Temflex", "Nivel de Burbuja Aluminio 24\"", "Candado TSA Programable Yale", "Compresora de Aire Truper 24L"
    };
    private static final String[] FERR_IMAGES = {
        "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600",
        "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600",
        "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600",
        "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600",
        "https://images.unsplash.com/photo-1558002038-1055907df827?w=600",
        "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600",
        "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600",
        "https://images.unsplash.com/photo-1558002038-1055907df827?w=600",
        "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600",
        "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600",
        "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600",
        "https://images.unsplash.com/photo-1558002038-1055907df827?w=600",
        "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600",
        "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600",
        "https://images.unsplash.com/photo-1558002038-1055907df827?w=600",
        "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600",
        "https://images.unsplash.com/photo-1558002038-1055907df827?w=600",
        "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600",
        "https://images.unsplash.com/photo-1558002038-1055907df827?w=600",
        "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600"
    };

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Roles if they don't exist
        Rol adminRol = seedRol(RolNombre.ADMIN);
        Rol vendedorRol = seedRol(RolNombre.VENDEDOR);
        Rol compradorRol = seedRol(RolNombre.COMPRADOR);

        // 2. Seed Users individually if they don't exist in the database
        boolean seeded = false;

        if (!usuarioRepository.existsByCorreo("admin@multimarket.com")) {
            seedUsuario("admin@multimarket.com", "admin123", "Admin", "General", "99999999", "11122233", adminRol);
            System.out.println("====== [DATA SEEDER] Usuario ADMIN registrado con éxito: admin@multimarket.com / admin123 ======");
            seeded = true;
        }

        if (!usuarioRepository.existsByCorreo("vendedor@multimarket.com")) {
            seedUsuario("vendedor@multimarket.com", "vendedor123", "Juan", "Vendedor", "987654321", "44455566", vendedorRol);
            System.out.println("====== [DATA SEEDER] Usuario VENDEDOR registrado con éxito: vendedor@multimarket.com / vendedor123 ======");
            seeded = true;
        }

        if (!usuarioRepository.existsByCorreo("comprador@multimarket.com")) {
            seedUsuario("comprador@multimarket.com", "comprador123", "Maria", "Comprador", "912345678", "77788899", compradorRol);
            System.out.println("====== [DATA SEEDER] Usuario COMPRADOR registrado con éxito: comprador@multimarket.com / comprador123 ======");
            seeded = true;
        }

        // 3. Seed the 6 Categories
        Categoria cafeCat = seedCategoria("Café", "Granos de café regionales y mezclas de altura.");
        Categoria chocoCat = seedCategoria("Chocolate", "Barras de chocolate, bombones y cacao en polvo.");
        Categoria arteCat = seedCategoria("Artesanías", "Cerámicas, platería, retablos y manualidades tradicionales.");
        Categoria textCat = seedCategoria("Textiles", "Mantados, chalinas, chompas de alpaca y prendas típicas.");
        Categoria mielCat = seedCategoria("Miel", "Miel de abeja pura y derivados apícolas.");
        Categoria ferrCat = seedCategoria("Ferretería", "Herramientas manuales, eléctricas y accesorios de construcción.");

        // 4. Seed the 20 Stores (Vendedores) and their Users
        String[][] storesData = {
            {"Cafetería del Centro", "Los mejores cafés orgánicos de Cusco tostados al natural.", "Cusco", "Portal de Panes 123", "Café", "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=100", "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=600"},
            {"Chocolates El Ceibo", "Cacao fino de aroma cosechado por comunidades locales.", "Amazonas", "Jr. Triunfo 456", "Chocolate", "https://images.unsplash.com/photo-1548907040-4d42b52125e0?w=100", "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=600"},
            {"Artesanías Andinas", "Textiles, cerámicas y platería hechos a mano.", "Puno", "Av. Floral 890", "Artesanías", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=100", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"},
            {"Ferretería Cleo", "Líderes en distribución de herramientas manuales, eléctricas, materiales de construcción, cerrajería, iluminación y acabados para el hogar. Más de 15 años brindando soluciones confiables a maestros de obra, talleres industriales, artesanos y familias de Lima Norte.", "Lima", "Av. Alfredo Mendiola 3540, Los Olivos, Lima", "Ferretería", "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=100", "https://images.unsplash.com/photo-1513828722001-c22dbf88279e?w=600"},
            {"Cusco Premium Café", "Café premium seleccionado artesanalmente de los valles del Cusco.", "Cusco", "Av. Sol 420, Cusco", "Café", "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=100", "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=600"},
            {"Dulce Amazonía", "Barras artesanales de chocolate con frutas exóticas del Amazonas.", "Amazonas", "Chachapoyas 789", "Chocolate", "https://images.unsplash.com/photo-1548907040-4d42b52125e0?w=100", "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=600"},
            {"Textil Cusco Imperial", "Chompas, mantas y chalinas tejidas con fina alpaca baby.", "Cusco", "Calle Hatun Rumiyoc 210, Cusco", "Textiles", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=100", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"},
            {"Apícola del Bosque", "Miel de abeja 100% pura recolectada de flores silvestres del norte.", "Lambayeque", "Av. Balta 654, Chiclayo", "Miel", "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=100", "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600"},
            {"Cerámicas Pucará", "Toritos de Pucará y artesanías pintadas a mano de alta calidad.", "Puno", "Jr. Lima 321, Puno", "Artesanías", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=100", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"},
            {"Café San Martín Gourmet", "Café de alta calidad con notas dulces y afrutadas de Moyobamba.", "San Martín", "Jr. San Martín 150, Moyobamba", "Café", "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=100", "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=600"},
            {"Chocolates Quillabamba", "Chocolate orgánico al 70% elaborado con cacao chuncho premium.", "Cusco", "Calle Espinar 450, Cusco", "Chocolate", "https://images.unsplash.com/photo-1548907040-4d42b52125e0?w=100", "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=600"},
            {"Orfebrería del Sur", "Joyas de plata de 950 hechas por experimentados plateros.", "Arequipa", "Calle Santa Catalina 111, Arequipa", "Artesanías", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=100", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"},
            {"Textil Altiplano", "Prendas típicas de abrigo tejidas con lana pura de oveja y alpaca.", "Puno", "Jr. Deustua 550, Puno", "Textiles", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=100", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"},
            {"Miel de La Libertad", "Miel pura y derivados apícolas como polen y jalea real.", "La Libertad", "Av. Larco 880, Trujillo", "Miel", "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=100", "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600"},
            {"Ferretería Norte", "Amplio catálogo de herramientas eléctricas profesionales y acabados.", "Piura", "Av. Grau 1200, Piura", "Ferretería", "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=100", "https://images.unsplash.com/photo-1513828722001-c22dbf88279e?w=600"},
            {"Café Chanchamayo", "Café cultivado en la selva central con un aroma inconfundible.", "Junín", "Av. Tarma 340, La Merced", "Café", "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=100", "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=600"},
            {"Granos Cajamarca", "Café gourmet producido bajo sombra en fincas cajamarquinas.", "Cajamarca", "Jr. Comercio 560, Cajamarca", "Café", "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=100", "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=600"},
            {"Artesanías de Piura", "Trabajos finos de filigrana de plata de Catacaos.", "Piura", "Jr. Comercio Catacaos 220", "Artesanías", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=100", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"},
            {"Textil Alpaca Real", "Colección de chompas de alpaca y accesorios de moda sostenible.", "Arequipa", "Calle Mercaderes 305, Arequipa", "Textiles", "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=100", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"},
            {"Miel Andina", "Miel multifloral orgánica de los valles sagrados.", "Cusco", "Urubamba Sector Central, Cusco", "Miel", "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=100", "https://images.unsplash.com/photo-1471943033881-a17e6a14e3d1?w=600"}
        };

        for (int i = 0; i < storesData.length; i++) {
            String[] sData = storesData[i];
            String storeName = sData[0];
            String storeDesc = sData[1];
            String region = sData[2];
            String address = sData[3];
            String catName = sData[4];
            String logo = sData[5];
            String banner = sData[6];

            // 1. Determine or seed vendedor user email
            String email = (i == 0) ? "vendedor@multimarket.com" : "vendedor" + (i + 1) + "@multimarket.com";
            Usuario vendorUser = seedVendedorUsuario(email, "vendedor123", "Socio", storeName, "9" + String.format("%08d", i), "20" + String.format("%06d", i), vendedorRol);

            // 2. Seed Vendedor store profile
            Vendedor vendedor = seedVendedor(vendorUser, storeName, storeDesc, region, address, logo, banner, i);

            // 3. Seed 20 products for this store
            Categoria associatedCategory = getCategoryByStr(catName, cafeCat, chocoCat, arteCat, textCat, mielCat, ferrCat);
            seedProductsForVendor(vendedor, associatedCategory, i + 1);
        }

        if (seeded) {
            System.out.println("\n=============================================================================");
            System.out.println("====== [DATA SEEDER] BASE DE DATOS INICIALIZADA CON USUARIOS DE PRUEBA ======");
            System.out.println("1. ADMIN: admin@multimarket.com / admin123");
            System.out.println("2. VENDEDORES: vendedor@multimarket.com a vendedor20@multimarket.com / vendedor123");
            System.out.println("3. COMPRADOR: comprador@multimarket.com / comprador123");
            System.out.println("=============================================================================\n");
        }
    }

    private Rol seedRol(RolNombre nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(nombre, "Rol " + nombre.name())));
    }

    private Categoria seedCategoria(String nombre, String descripcion) {
        return categoriaRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Categoria cat = new Categoria();
                    cat.setNombre(nombre);
                    cat.setDescripcion(descripcion);
                    cat.setActiva(true);
                    return categoriaRepository.save(cat);
                });
    }

    private Usuario seedVendedorUsuario(String correo, String password, String nombres, String apellidos, String telefono, String dni, Rol rol) {
        return usuarioRepository.findByCorreo(correo)
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    usuario.setCorreo(correo);
                    usuario.setPassword(passwordEncoder.encode(password));
                    usuario.setEstado(true);
                    usuario.setCorreoVerificado(true);
                    usuario.setIntentosFallidos(0);
                    usuario.setBloqueado(false);
                    usuario.setFechaRegistro(LocalDateTime.now());

                    Set<Rol> roles = new HashSet<>();
                    roles.add(rol);
                    usuario.setRoles(roles);

                    Perfil perfil = new Perfil();
                    perfil.setNombres(nombres);
                    perfil.setApellidos(apellidos);
                    perfil.setDni(dni);
                    perfil.setTelefono(telefono);
                    perfil.setDireccion("Av. Siempreviva 742");
                    perfil.setFechaNacimiento(LocalDate.of(1990, 1, 1));
                    usuario.setPerfil(perfil);

                    return usuarioRepository.save(usuario);
                });
    }

    private void seedUsuario(String correo, String password, String nombres, String apellidos, String telefono, String dni, Rol rol) {
        seedVendedorUsuario(correo, password, nombres, apellidos, telefono, dni, rol);
    }

    private Vendedor seedVendedor(Usuario user, String nombreTienda, String descripcion, String region, String direccion, String logo, String banner, int index) {
        return vendedorRepository.findByUsuarioCorreo(user.getCorreo())
                .orElseGet(() -> {
                    Vendedor vendedor = new Vendedor();
                    vendedor.setUsuario(user);
                    vendedor.setNombreTienda(nombreTienda);
                    vendedor.setDescripcion(descripcion);
                    vendedor.setRegion(region);
                    vendedor.setDireccion(direccion);
                    vendedor.setLogo(logo);
                    vendedor.setBanner(banner);
                    vendedor.setActivo(true);
                    vendedor.setCalificacionPromedio(BigDecimal.valueOf(4.5 + (index % 5) * 0.1));
                    vendedor.setFechaCreacion(LocalDateTime.now());
                    return vendedorRepository.save(vendedor);
                });
    }

    private Categoria getCategoryByStr(String catName, Categoria cafe, Categoria choco, Categoria arte, Categoria text, Categoria miel, Categoria ferr) {
        switch (catName) {
            case "Chocolate": return choco;
            case "Artesanías": return arte;
            case "Textiles": return text;
            case "Miel": return miel;
            case "Ferretería": return ferr;
            default: return cafe;
        }
    }

    private void seedProductsForVendor(Vendedor vendedor, Categoria category, int storeNum) {
        String[] poolNames;
        String[] poolImages;

        if (category.getNombre().equals("Chocolate")) {
            poolNames = CHOCO_NAMES;
            poolImages = CHOCO_IMAGES;
        } else if (category.getNombre().equals("Artesanías")) {
            poolNames = ART_NAMES;
            poolImages = ART_IMAGES;
        } else if (category.getNombre().equals("Textiles")) {
            poolNames = TEXT_NAMES;
            poolImages = TEXT_IMAGES;
        } else if (category.getNombre().equals("Miel")) {
            poolNames = MIEL_NAMES;
            poolImages = MIEL_IMAGES;
        } else if (category.getNombre().equals("Ferretería")) {
            poolNames = FERR_NAMES;
            poolImages = FERR_IMAGES;
        } else {
            poolNames = CAFE_NAMES;
            poolImages = CAFE_IMAGES;
        }

        for (int i = 0; i < 20; i++) {
            String name = vendedor.getNombreTienda() + " - " + poolNames[i];
            String sku = "SKU-" + String.format("%03d", storeNum) + "-" + String.format("%02d", i + 1);

            if (productoRepository.findBySku(sku).isEmpty()) {
                Producto prod = new Producto();
                prod.setNombre(name);
                prod.setDescripcion("Producto artesanal premium de la tienda " + vendedor.getNombreTienda() + ". Cultivado o elaborado siguiendo tradiciones locales de la región de " + vendedor.getRegion() + ".");
                prod.setSku(sku);
                prod.setPrecio(BigDecimal.valueOf(15.00 + (i * 3.5) + (storeNum * 1.5)));
                prod.setStock(30 - i);
                prod.setPeso(BigDecimal.valueOf(0.5 + (i * 0.05)));
                prod.setActivo(true);
                prod.setCategoria(category);
                prod.setVendedor(vendedor);

                // Save product first so it gets an ID
                Producto savedProd = productoRepository.save(prod);

                // Add image in cascade
                ImagenProducto img = new ImagenProducto();
                img.setUrl(poolImages[i]);
                img.setPrincipal(true);
                img.setOrdenVisualizacion(1);
                img.setProducto(savedProd);

                List<ImagenProducto> imgs = new ArrayList<>();
                imgs.add(img);
                savedProd.setImagenes(imgs);
                productoRepository.save(savedProd);

                // Add and save Inventario
                Inventario inv = new Inventario();
                inv.setStockActual(savedProd.getStock());
                inv.setStockMinimo(5);
                inv.setProducto(savedProd);
                inv.setUltimaActualizacion(LocalDateTime.now());
                inventarioRepository.save(inv);
            }
        }
    }
}
