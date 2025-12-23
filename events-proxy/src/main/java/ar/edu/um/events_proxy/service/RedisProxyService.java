package ar.edu.um.events_proxy.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.um.events_proxy.dto.EventSeatsDTO;
import ar.edu.um.events_proxy.dto.SeatDTO;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class RedisProxyService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisProxyService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Obtiene los asientos bloqueados/vendidos de un evento desde Redis
     * @param eventId ID del evento
     * @return JSON con los asientos ocupados, o null si no existe
     */
  public String getSeats(String eventId) {
    try {
        String key = "evento_" + eventId;
        String value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }
        
        EventSeatsDTO eventSeats = objectMapper.readValue(value, EventSeatsDTO.class);
        
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        
        for (SeatDTO seat : eventSeats.getAsientos()) {
            if (seat.getExpira() != null) {
                ZonedDateTime expiraTime = ZonedDateTime.parse(seat.getExpira());
                
                long minutesPassed = ChronoUnit.MINUTES.between(expiraTime, now);
                
                if (minutesPassed > 5) {
                    seat.setEstado("libre");
                    seat.setExpira(null);
                }
            }
        }
        
        return objectMapper.writeValueAsString(eventSeats);
    } catch (Exception e) {
        throw new RuntimeException("Error al leer datos de Redis", e);
    }
}
}