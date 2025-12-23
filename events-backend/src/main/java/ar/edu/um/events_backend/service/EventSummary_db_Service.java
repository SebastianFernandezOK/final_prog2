package ar.edu.um.events_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ar.edu.um.events_backend.auth.ExternalAuthService;
import ar.edu.um.events_backend.dto.Event_summary_db_DTO;
import ar.edu.um.events_backend.entity.EventSummary_db;
import ar.edu.um.events_backend.repository.EventSummary_db_Repository;

@Service
public class EventSummary_db_Service {
    
    private static final Logger logger = LoggerFactory.getLogger(EventSummary_db_Service.class);
    
    private final EventSummary_db_Repository repository;
    private final WebClient webClient;
    private final ExternalAuthService externalAuthService;
    private final Event_db_Service eventDbService;
    
    @Value("${externalAuth.base-url}")
    private String externalBaseUrl;

    public EventSummary_db_Service(EventSummary_db_Repository repository,
                                    @Qualifier("externalApiClient") WebClient webClient,
                                    ExternalAuthService externalAuthService,
                                    Event_db_Service eventDbService) {
        this.repository = repository;
        this.webClient = webClient;
        this.externalAuthService = externalAuthService;
        this.eventDbService = eventDbService;
    }

    public List<EventSummary_db> findAll() {
        return repository.findAll();
    }

    public Optional<EventSummary_db> findById(Long id) {
        return repository.findById(id);
    }

    public EventSummary_db save(EventSummary_db event) {
        return repository.save(event);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    /**
     * Sincroniza todos los eventos desde el servicio externo de la cátedra
     * y actualiza la base de datos local (tanto resumidos como completos).
     */
    public void syncAllEventsFromExternal() {
        logger.info("Iniciando sincronización completa de eventos desde servicio externo");
        
        // Sincronizar eventos resumidos
        syncEventsSummary();
        
        // Sincronizar eventos completos
        eventDbService.syncAllFullEventsFromExternal();
        
        logger.info("Sincronización completa de eventos finalizada");
    }
    
    /**
     * Sincroniza solo los eventos resumidos
     */
    private void syncEventsSummary() {
        logger.info("Iniciando sincronización de eventos resumidos desde servicio externo");
        
        try {
            // Llamar al servicio externo
            List<Event_summary_db_DTO> externalEvents = webClient.get()
                    .uri(externalBaseUrl + "/api/endpoints/v1/eventos-resumidos")
                    .header("Authorization", "Bearer " + externalAuthService.getToken())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Event_summary_db_DTO>>() {})
                    .block();
            
            if (externalEvents == null || externalEvents.isEmpty()) {
                logger.warn("No se recibieron eventos del servicio externo");
                return;
            }
            
            logger.info("Recibidos {} eventos del servicio externo", externalEvents.size());
            
            // 1. Obtener IDs de eventos externos
            Set<Long> externalIds = externalEvents.stream()
                    .map(Event_summary_db_DTO::getId)
                    .collect(Collectors.toSet());
            
            // 2. Borrar eventos locales que ya no existen en el externo
            List<EventSummary_db> localEvents = repository.findAll();
            int deletedCount = 0;
            for (EventSummary_db local : localEvents) {
                if (!externalIds.contains(local.getId())) {
                    repository.deleteById(local.getId());
                    deletedCount++;
                    logger.debug("Evento eliminado (ya no existe en servicio externo): ID={}", local.getId());
                }
            }
            if (deletedCount > 0) {
                logger.info("Eliminados {} eventos que ya no existen en el servicio externo", deletedCount);
            }
            
            // 3. Insertar/actualizar todos los eventos del servicio externo
            for (Event_summary_db_DTO dto : externalEvents) {
                EventSummary_db entity = mapDtoToEntity(dto);
                repository.save(entity);
                logger.debug("Evento sincronizado: ID={}, Titulo={}", entity.getId(), entity.getTitulo());
            }
            
            logger.info("Sincronización de eventos resumidos completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al sincronizar eventos resumidos desde servicio externo", e);
            throw new RuntimeException("Error en sincronización de eventos resumidos", e);
        }
    }
    
    /**
     * Mapea un EventDTO del servicio externo a la entidad EventSummary_db
     */
    private EventSummary_db mapDtoToEntity(Event_summary_db_DTO dto) {
        EventSummary_db entity = new EventSummary_db();
        entity.setId(dto.getId());
        entity.setTitulo(dto.getTitulo());
        entity.setResumen(dto.getResumen());
        entity.setDescripcion(dto.getDescripcion());
        entity.setFecha(dto.getFecha());
        entity.setPrecioEntrada(dto.getPrecioEntrada());
        
        if (dto.getEventoTipo() != null) {
            entity.setEventoTipoNombre(dto.getEventoTipo().getNombre());
            entity.setEventoTipoDescripcion(dto.getEventoTipo().getDescripcion());
        }
        
        return entity;
    }
}
