package ar.edu.um.events_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.events_backend.service.RedisService;

@RestController
@RequestMapping("/api/db/events")
public class RedisController {
    
    private final RedisService redisService;
    
    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }
    
    /**
     * Obtiene todos los asientos de un evento
     * GET /api/db/events/{eventId}/seats
     * @param eventId ID del evento
     * @return JSON con todos los asientos y sus estados
     */
    @GetMapping("/{eventId}/seats")
    public ResponseEntity<String> getSeats(@PathVariable Long eventId) {
        try {
            String seats = redisService.getSeatsForEvent(eventId);
            
            if (seats == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(seats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Obtiene un asiento específico de un evento
     * GET /api/db/events/{eventId}/seats/{row}/{col}
     * @param eventId ID del evento
     * @param row Fila del asiento
     * @param col Columna del asiento
     * @return JSON con el estado del asiento específico
     */
    @GetMapping("/{eventId}/seats/{row}/{col}")
    public ResponseEntity<String> getSeat(@PathVariable Long eventId, 
                                          @PathVariable int row, 
                                          @PathVariable int col) {
        try {
            String seat = redisService.getSeatDetail(eventId, row, col);
            
            if (seat == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(seat);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
