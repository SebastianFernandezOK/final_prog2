package ar.edu.um.events_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RedisService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    
    private final WebClient proxyClient;
    private final ObjectMapper objectMapper;
    
    public RedisService(@Qualifier("proxyClient") WebClient proxyClient, ObjectMapper objectMapper) {
        this.proxyClient = proxyClient;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Obtiene los asientos de un evento desde Redis a través del proxy
     * @param eventId ID del evento
     * @return JSON con los asientos
     */
    public String getSeatsForEvent(Long eventId) {
        logger.info("Solicitando asientos del evento {} al proxy", eventId);
        
        try {
            String response = proxyClient
                .get()
                .uri("/proxy/redis/seats/{eventId}", eventId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            logger.info("Asientos recibidos del proxy para evento {}", eventId);
            return response;
            
        } catch (Exception e) {
            logger.error("Error al obtener asientos del evento {} desde el proxy: {}", eventId, e.getMessage());
            throw new RuntimeException("Error al consultar asientos del evento", e);
        }
    }
    
    /**
     * Obtiene un asiento específico de un evento
     * @param eventId ID del evento
     * @param row Fila del asiento
     * @param col Columna del asiento
     * @return JSON con el asiento específico
     */
    public String getSeatDetail(Long eventId, int row, int col) {
        logger.info("Solicitando asiento [{},{}] del evento {} al proxy", row, col, eventId);
        
        try {
            // Devolver la misma respuesta que getSeatsForEvent sin mapear
            return getSeatsForEvent(eventId);
            
        } catch (Exception e) {
            logger.error("Error al obtener asiento [{},{}] del evento {}: {}", row, col, eventId, e.getMessage());
            throw new RuntimeException("Error al consultar asiento específico", e);
        }
    }
}
