package ar.edu.um.events_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ar.edu.um.events_backend.dto.Sale_summary_DTO;
import ar.edu.um.events_backend.entity.Sale_db;
import ar.edu.um.events_backend.repository.Sale_db_Repository;

@Service
public class Sale_db_Service {
    
    private static final Logger logger = LoggerFactory.getLogger(Sale_db_Service.class);
    
    private final Sale_db_Repository repository;
    private final WebClient externalApiClient;

    public Sale_db_Service(Sale_db_Repository repository,
                          @Qualifier("externalApiClient") WebClient externalApiClient) {
        this.repository = repository;
        this.externalApiClient = externalApiClient;
    }

    public List<Sale_db> findAll() {
        return repository.findAll();
    }

    public Optional<Sale_db> findById(Long ventaId) {
        return repository.findById(ventaId);
    }
    
    public List<Sale_db> findByEventoId(Long eventoId) {
        return repository.findByEventoId(eventoId);
    }
    
    public void syncSalesFromExternal() {
        logger.info("Iniciando sincronización de ventas desde servicio externo");
        
        try {
            List<Sale_summary_DTO> externalSales = externalApiClient.get()
                    .uri("/api/endpoints/v1/listar-ventas")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Sale_summary_DTO>>() {})
                    .block();
            
            if (externalSales == null || externalSales.isEmpty()) {
                logger.warn("No se recibieron ventas del servicio externo");
                return;
            }
            
            logger.info("Recibidas {} ventas del servicio externo", externalSales.size());
            
            Set<Long> externalIds = externalSales.stream()
                    .map(Sale_summary_DTO::getVentaId)
                    .collect(Collectors.toSet());
            
            List<Sale_db> localSales = repository.findAll();
            int deletedCount = 0;
            for (Sale_db local : localSales) {
                if (!externalIds.contains(local.getVentaId())) {
                    repository.deleteById(local.getVentaId());
                    deletedCount++;
                    logger.debug("Venta eliminada (ya no existe en cátedra): ventaId={}", local.getVentaId());
                }
            }
            if (deletedCount > 0) {
                logger.info("Eliminadas {} ventas que ya no existen en cátedra", deletedCount);
            }
            
            for (Sale_summary_DTO dto : externalSales) {
                Sale_db entity = mapDtoToEntity(dto);
                repository.save(entity);
                logger.debug("Venta sincronizada: ventaId={}, resultado={}", entity.getVentaId(), entity.isResultado());
            }
            
            logger.info("Sincronización de ventas completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al sincronizar ventas desde servicio externo", e);
        }
    }
    
    private Sale_db mapDtoToEntity(Sale_summary_DTO dto) {
        Sale_db entity = new Sale_db();
        entity.setVentaId(dto.getVentaId());
        entity.setEventoId(dto.getEventoId());
        entity.setFechaVenta(dto.getFechaVenta());
        entity.setResultado(dto.isResultado());
        entity.setDescripcion(dto.getDescripcion());
        entity.setPrecioVenta(dto.getPrecioVenta());
        entity.setCantidadAsientos(dto.getCantidadAsientos());
        entity.setAsientosJson(null);
        return entity;
    }
}
