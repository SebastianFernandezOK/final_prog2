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
import ar.edu.um.events_backend.dto.Event_db_DTO;
import ar.edu.um.events_backend.entity.Event_db;
import ar.edu.um.events_backend.repository.Event_db_Repository;

@Service
public class Event_db_Service {
    
    private static final Logger logger = LoggerFactory.getLogger(Event_db_Service.class);
    
    private final Event_db_Repository repository;
    private final WebClient webClient;
    private final ExternalAuthService externalAuthService;
    
    @Value("${externalAuth.base-url}")
    private String externalBaseUrl;

    public Event_db_Service(Event_db_Repository repository,
                            @Qualifier("externalApiClient") WebClient webClient,
                            ExternalAuthService externalAuthService) {
        this.repository = repository;
        this.webClient = webClient;
        this.externalAuthService = externalAuthService;
    }

    public List<Event_db> findAll() {
        return repository.findAll();
    }

    public Optional<Event_db> findById(Long id) {
        return repository.findById(id);
    }

    public Event_db save(Event_db event) {
        return repository.save(event);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    /**
     * Sincroniza todos los eventos completos desde el servicio externo
     */
    public void syncAllFullEventsFromExternal() {
        logger.info("Iniciando sincronización de eventos completos desde servicio externo");
        
        try {
            // Llamar al servicio externo
            List<Event_db_DTO> externalEvents = webClient.get()
                    .uri(externalBaseUrl + "/api/endpoints/v1/eventos")
                    .header("Authorization", "Bearer " + externalAuthService.getToken())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Event_db_DTO>>() {})
                    .block();
            
            if (externalEvents == null || externalEvents.isEmpty()) {
                logger.warn("No se recibieron eventos completos del servicio externo");
                return;
            }
            
            logger.info("Recibidos {} eventos completos del servicio externo", externalEvents.size());
            
            // 1. Obtener IDs de eventos externos
            Set<Long> externalIds = externalEvents.stream()
                    .map(Event_db_DTO::getId)
                    .collect(Collectors.toSet());
            
            // 2. Borrar eventos locales que ya no existen en el externo
            List<Event_db> localEvents = repository.findAll();
            int deletedCount = 0;
            for (Event_db local : localEvents) {
                if (!externalIds.contains(local.getId())) {
                    repository.deleteById(local.getId());
                    deletedCount++;
                    logger.debug("Evento completo eliminado (ya no existe en servicio externo): ID={}", local.getId());
                }
            }
            if (deletedCount > 0) {
                logger.info("Eliminados {} eventos completos que ya no existen en el servicio externo", deletedCount);
            }
            
            // 3. Insertar/actualizar todos los eventos del servicio externo
            for (Event_db_DTO dto : externalEvents) {
                Event_db entity = mapFullDtoToEntity(dto);
                repository.save(entity);
                logger.debug("Evento completo sincronizado: ID={}, Titulo={}", entity.getId(), entity.getTitulo());
            }
            
            logger.info("Sincronización de eventos completos completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al sincronizar eventos completos desde servicio externo", e);
            throw new RuntimeException("Error en sincronización de eventos completos", e);
        }
    }
    
    /**
     * Mapea un EventFullDTO del servicio externo a la entidad Event_db
     */
    private Event_db mapFullDtoToEntity(Event_db_DTO dto) {
        Event_db entity = new Event_db();
        entity.setId(dto.getId());
        entity.setTitulo(dto.getTitulo());
        entity.setResumen(dto.getResumen());
        entity.setDescripcion(dto.getDescripcion());
        entity.setFecha(dto.getFecha());
        entity.setDireccion(dto.getDireccion());
        entity.setImagen(dto.getImagen());
        entity.setFilaAsientos(dto.getFilaAsientos());
        entity.setColumnAsientos(dto.getColumnAsientos());
        entity.setPrecioEntrada(dto.getPrecioEntrada());
        
        if (dto.getEventoTipo() != null) {
            entity.setEventoTipoNombre(dto.getEventoTipo().getNombre());
            entity.setEventoTipoDescripcion(dto.getEventoTipo().getDescripcion());
        }
        
        // Mapear integrantes
        if (dto.getIntegrantes() != null) {
            List<Event_db.Integrante> integrantes = dto.getIntegrantes().stream()
                    .map(integranteDTO -> {
                        Event_db.Integrante integrante = new Event_db.Integrante();
                        integrante.setNombre(integranteDTO.getNombre());
                        integrante.setApellido(integranteDTO.getApellido());
                        integrante.setIdentificacion(integranteDTO.getIdentificacion());
                        return integrante;
                    })
                    .collect(Collectors.toList());
            entity.setIntegrantes(integrantes);
        }
        
        return entity;
    }
}
