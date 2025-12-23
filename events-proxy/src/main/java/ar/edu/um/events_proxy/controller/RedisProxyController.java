package ar.edu.um.events_proxy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.events_proxy.service.RedisProxyService;

@RestController
@RequestMapping("/proxy/redis")
public class RedisProxyController {

    private final RedisProxyService redisProxyService;

    public RedisProxyController(RedisProxyService redisProxyService) {
        this.redisProxyService = redisProxyService;
    }

    /**
     * Gets blocked/sold seats for an event from Redis
     * @param eventId Event ID
     * @return JSON with occupied seats
     */
    @GetMapping("/seats/{eventId}")
    public ResponseEntity<String> getSeats(@PathVariable String eventId) {
        System.out.println("🔵 PROXY: Recibida petición del backend para eventId: " + eventId);
        
        String seats = redisProxyService.getSeats(eventId);
        
        if (seats == null) {
            System.out.println("🔴 PROXY: Respondiendo al backend - No encontrado (404) para eventId: " + eventId);
            return ResponseEntity.notFound().build();
        }
        
        System.out.println("🟢 PROXY: Respondiendo al backend - Datos encontrados para eventId: " + eventId);
        System.out.println("   Datos: " + seats);
        return ResponseEntity.ok(seats);
    }
}
