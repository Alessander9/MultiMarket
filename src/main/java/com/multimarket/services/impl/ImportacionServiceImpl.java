package com.multimarket.services.impl;

import com.multimarket.dto.CatalogoXml;
import com.multimarket.dto.ProductoXml;
import com.multimarket.models.*;
import com.multimarket.repositories.*;
import com.multimarket.services.Interfaces.ImportacionService;
import com.multimarket.kafka.KafkaProducer;
import com.multimarket.kafka.events.StockActualizadoEvent;
import com.multimarket.kafka.events.LogGeneradoEvent;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Transactional
public class ImportacionServiceImpl implements ImportacionService {

    private static final Logger LOGGER = Logger.getLogger(ImportacionServiceImpl.class.getName());
    private static final String UPLOAD_DIR = "uploads/xml/";

    private final ImportacionCatalogoRepository importacionRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VendedorRepository vendedorRepository;
    private final InventarioRepository inventarioRepository;
    private final KafkaProducer kafkaProducer;

    public ImportacionServiceImpl(ImportacionCatalogoRepository importacionRepository,
                                  ProductoRepository productoRepository,
                                  CategoriaRepository categoriaRepository,
                                  VendedorRepository vendedorRepository,
                                  InventarioRepository inventarioRepository,
                                  KafkaProducer kafkaProducer) {
        this.importacionRepository = importacionRepository;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.vendedorRepository = vendedorRepository;
        this.inventarioRepository = inventarioRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public ImportacionCatalogo importarCatalogoXml(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo XML está vacío o no es válido.");
        }

        ImportacionCatalogo importacion = new ImportacionCatalogo();
        importacion.setNombreArchivo(file.getOriginalFilename());
        
        File tempFile = null;
        try {
            // 1. Crear directorio de uploads si no existe y guardar archivo
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String uniqueName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + uniqueName);
            Files.write(path, file.getBytes());
            
            importacion.setRutaArchivo(path.toAbsolutePath().toString());
            tempFile = path.toFile();

            // 2. Procesar JAXB
            JAXBContext context = JAXBContext.newInstance(CatalogoXml.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            CatalogoXml catalogo = (CatalogoXml) unmarshaller.unmarshal(tempFile);

            int total = catalogo.getProductos().size();
            int correctos = 0;
            int errores = 0;

            // 3. Insertar productos
            for (ProductoXml prodXml : catalogo.getProductos()) {
                try {
                    // Validar SKU único
                    if (productoRepository.findBySku(prodXml.getSku()).isPresent()) {
                        throw new IllegalArgumentException("El SKU '" + prodXml.getSku() + "' ya está registrado en el sistema.");
                    }

                    // Validar categoría
                    Categoria categoria = categoriaRepository.findById(prodXml.getCategoriaId())
                            .orElseThrow(() -> new IllegalArgumentException("Categoría con ID " + prodXml.getCategoriaId() + " no encontrada."));

                    // Validar vendedor
                    Vendedor vendedor = vendedorRepository.findById(prodXml.getVendedorId())
                            .orElseThrow(() -> new IllegalArgumentException("Vendedor con ID " + prodXml.getVendedorId() + " no encontrado."));

                    // Guardar producto
                    Producto prod = new Producto();
                    prod.setNombre(prodXml.getNombre());
                    prod.setDescripcion(prodXml.getDescripcion());
                    prod.setSku(prodXml.getSku());
                    prod.setPrecio(prodXml.getPrecio());
                    prod.setStock(prodXml.getStock());
                    prod.setPeso(prodXml.getPeso());
                    prod.setCategoria(categoria);
                    prod.setVendedor(vendedor);
                    prod.setActivo(true);
                    prod.setFechaCreacion(LocalDateTime.now());
                    prod.setFechaActualizacion(LocalDateTime.now());
                    
                    Producto savedProd = productoRepository.save(prod);

                    // Inicializar inventario asociado
                    Inventario inv = new Inventario();
                    inv.setProducto(savedProd);
                    inv.setStockActual(prodXml.getStock());
                    inv.setStockMinimo(5);
                    inv.setUltimaActualizacion(LocalDateTime.now());
                    inventarioRepository.save(inv);

                    // Notificar Kafka Evento de StockActualizado
                    kafkaProducer.sendInventarioEvent(new StockActualizadoEvent(
                            savedProd.getId(),
                            0,
                            prodXml.getStock(),
                            "ENTRADA",
                            prodXml.getStock()
                    ));

                    correctos++;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error al importar un producto: " + ex.getMessage());
                    errores++;
                }
            }

            importacion.setTotalRegistros(total);
            importacion.setRegistrosCorrectos(correctos);
            importacion.setRegistrosError(errores);

            // Audit Log Event via Kafka
            kafkaProducer.sendLogEvent(new LogGeneradoEvent(
                    "INFO",
                    "PRODUCTO",
                    "IMPORTACION_XML",
                    "Importación de catálogo XML completada. Correctos: " + correctos + ", Errores: " + errores,
                    "localhost",
                    "/importar",
                    "POST",
                    null,
                    "",
                    "Total: " + total,
                    "",
                    true
            ));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fallo crítico al unmarshallear archivo XML: ", e);
            importacion.setTotalRegistros(0);
            importacion.setRegistrosCorrectos(0);
            importacion.setRegistrosError(1);
            
            // Audit Log Event for failure
            kafkaProducer.sendLogEvent(new LogGeneradoEvent(
                    "ERROR",
                    "PRODUCTO",
                    "IMPORTACION_XML",
                    "Fallo al procesar JAXB XML: " + e.getMessage(),
                    "localhost",
                    "/importar",
                    "POST",
                    null,
                    "",
                    "",
                    e.toString(),
                    false
            ));
            
            throw new RuntimeException("Error en la importación XML: " + e.getMessage());
        }

        return importacionRepository.save(importacion);
    }
}
