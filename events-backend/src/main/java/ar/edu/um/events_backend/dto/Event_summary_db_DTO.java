package ar.edu.um.events_backend.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class Event_summary_db_DTO {
    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private ZonedDateTime fecha;
    private BigDecimal precioEntrada;
    private EventoTipoDTO eventoTipo;
    
    @Data
    public static class EventoTipoDTO {
        private String nombre;
        private String descripcion;
    }
}
