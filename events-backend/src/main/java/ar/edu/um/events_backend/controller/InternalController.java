package ar.edu.um.events_backend.controller;

import ar.edu.um.events_backend.service.EventSummary_db_Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private static final Logger logger = LoggerFactory.getLogger(InternalController.class);
    
    private final EventSummary_db_Service eventSummaryService;

    public InternalController(EventSummary_db_Service eventSummaryService) {
        this.eventSummaryService = eventSummaryService;
    }

    /**
     * Endpoint interno para recibir notificaciones del proxy cuando Kafka detecta cambios.
     * Al recibir cualquier notificación, sincroniza todos los eventos desde el servicio externo.
     */
    @PostMapping("/events/sync")
    public ResponseEntity<String> syncEvents(@RequestBody(required = false) String message) {
        logger.info("Recibida notificación de sincronización desde proxy");
        if (message != null && !message.isEmpty()) {
            logger.info("Mensaje recibido: {}", message);
        }
        
        try {
            eventSummaryService.syncAllEventsFromExternal();
            return ResponseEntity.ok("Sincronización de eventos completada exitosamente");
        } catch (Exception e) {
            logger.error("Error al procesar sincronización de eventos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error al sincronizar eventos: " + e.getMessage());
        }
    }
}
