package com.multimarket.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multimarket.dto.CatalogoXml;
import com.multimarket.dto.ProductoXml;
import com.multimarket.models.*;
import com.multimarket.repositories.ExportacionCatalogoRepository;
import com.multimarket.repositories.ProductoRepository;
import com.multimarket.services.Interfaces.ExportacionService;
import com.multimarket.kafka.KafkaProducer;
import com.multimarket.kafka.events.LogGeneradoEvent;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ExportacionServiceImpl implements ExportacionService {

    private static final Logger LOGGER = Logger.getLogger(ExportacionServiceImpl.class.getName());
    private static final String EXPORT_DIR = "exports/";

    private final ExportacionCatalogoRepository exportacionRepository;
    private final ProductoRepository productoRepository;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    // ExecutorService de Java para tareas de exportación en segundo plano sin bloquear hilos HTTP
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public ExportacionServiceImpl(ExportacionCatalogoRepository exportacionRepository,
                                  ProductoRepository productoRepository,
                                  KafkaProducer kafkaProducer) {
        this.exportacionRepository = exportacionRepository;
        this.productoRepository = productoRepository;
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional
    public ExportacionCatalogo programarExportacion(FormatoExportacion formato) {
        // 1. Guardar la solicitud de exportación en estado PENDIENTE
        ExportacionCatalogo exportacion = new ExportacionCatalogo();
        exportacion.setFormato(formato);
        exportacion.setEstado(EstadoExportacion.PENDIENTE);
        exportacion.setFechaExportacion(LocalDateTime.now());
        
        ExportacionCatalogo savedExport = exportacionRepository.save(exportacion);

        // 2. Enviar a segundo plano de forma no bloqueante utilizando ExecutorService
        executorService.submit(() -> ejecutarExportacionAsincrona(savedExport.getId(), formato));

        LOGGER.log(Level.INFO, "[EXPORT-SERVICE] Programada la exportación ID {0} en formato {1}", 
                new Object[]{savedExport.getId(), formato});

        return savedExport;
    }

    private void ejecutarExportacionAsincrona(Long exportacionId, FormatoExportacion formato) {
        LOGGER.log(Level.INFO, "[EXPORT-THREAD] Iniciando exportación asíncrona ID: {0}", exportacionId);
        
        // 1. Obtener y actualizar estado a PROCESANDO
        ExportacionCatalogo exportacion = exportacionRepository.findById(exportacionId).orElse(null);
        if (exportacion == null) return;

        exportacion.setEstado(EstadoExportacion.PROCESANDO);
        exportacionRepository.save(exportacion);

        try {
            // Asegurar directorio
            File dir = new File(EXPORT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. Obtener los productos de base de datos
            // Obtenemos los productos y los convertimos en DTOs planos para evitar problemas de LazyLoading en el Thread independiente
            List<Producto> productos = productoRepository.findAll();
            List<ProductoXml> flatProducts = new ArrayList<>();
            for (Producto p : productos) {
                ProductoXml px = new ProductoXml();
                px.setNombre(p.getNombre());
                px.setDescripcion(p.getDescripcion());
                px.setSku(p.getSku());
                px.setPrecio(p.getPrecio());
                px.setStock(p.getStock());
                px.setPeso(p.getPeso());
                px.setCategoriaId(p.getCategoria().getId());
                px.setVendedorId(p.getVendedor().getId());
                flatProducts.add(px);
            }

            String filename = "catalogo_" + exportacionId + "_" + System.currentTimeMillis();
            String filepath = "";

            if (formato == FormatoExportacion.JSON) {
                filepath = EXPORT_DIR + filename + ".json";
                File file = new File(filepath);
                
                // Serializar a JSON usando Jackson
                objectMapper.writeValue(file, flatProducts);
                
            } else if (formato == FormatoExportacion.XML) {
                filepath = EXPORT_DIR + filename + ".xml";
                File file = new File(filepath);

                // Serializar a XML usando JAXB
                CatalogoXml catalogo = new CatalogoXml();
                catalogo.setProductos(flatProducts);

                JAXBContext context = JAXBContext.newInstance(CatalogoXml.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(catalogo, file);
            }

            // 3. Actualizar a COMPLETADO
            exportacion.setRutaArchivo(new File(filepath).getAbsolutePath());
            exportacion.setEstado(EstadoExportacion.COMPLETADO);
            exportacionRepository.save(exportacion);

            LOGGER.log(Level.INFO, "[EXPORT-THREAD] Exportación ID {0} finalizada correctamente.", exportacionId);

            // Kafka Event
            kafkaProducer.sendLogEvent(new LogGeneradoEvent(
                    "INFO",
                    "PRODUCTO",
                    "EXPORTACION",
                    "Exportación " + formato + " ID: " + exportacionId + " completada con éxito.",
                    "localhost",
                    "/exportar",
                    "GET",
                    null,
                    "",
                    "",
                    "",
                    true
            ));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[EXPORT-THREAD] Error crítico al procesar la exportación: ", e);
            
            exportacion.setEstado(EstadoExportacion.ERROR);
            exportacionRepository.save(exportacion);

            // Kafka Event
            kafkaProducer.sendLogEvent(new LogGeneradoEvent(
                    "ERROR",
                    "PRODUCTO",
                    "EXPORTACION",
                    "Error al procesar exportación " + formato + " ID " + exportacionId + ": " + e.getMessage(),
                    "localhost",
                    "/exportar",
                    "GET",
                    null,
                    "",
                    "",
                    e.toString(),
                    false
            ));
        }
    }
}
