package ar.edu.um.events_backend.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Event_db_DTO {
    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private ZonedDateTime fecha;
    private String direccion;
    private String imagen;
    private Integer filaAsientos;
    private Integer columnAsientos;
    private BigDecimal precioEntrada;
    private EventoTipoDTO eventoTipo;
    private List<IntegranteDTO> integrantes;
    
    @Data
    public static class EventoTipoDTO {
        private String nombre;
        private String descripcion;
    }
    
    @Data
    public static class IntegranteDTO {
        private String nombre;
        private String apellido;
        private String identificacion;
    }
}
