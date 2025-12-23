package ar.edu.um.events_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import ar.edu.um.events_backend.service.EventSummary_db_Service;

//@Component
public class EventsInitialLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(EventsInitialLoader.class);
    
    private final EventSummary_db_Service eventSummaryService;

    public EventsInitialLoader(EventSummary_db_Service eventSummaryService) {
        this.eventSummaryService = eventSummaryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Iniciando carga inicial de eventos al arrancar la aplicación");
        try {
            eventSummaryService.syncAllEventsFromExternal();
            logger.info("Carga inicial de eventos completada");
        } catch (Exception e) {
            logger.error("Error en la carga inicial de eventos: {}", e.getMessage(), e);
            // No lanzamos excepción para que la app arranque igual
        }
    }
}
