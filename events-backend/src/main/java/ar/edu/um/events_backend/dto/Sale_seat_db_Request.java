package ar.edu.um.events_backend.dto;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Sale_seat_db_Request {
    private Long eventoId;
    private ZonedDateTime fecha;
    private Double precioVenta;
    private List<AsientoVenta> asientos;

    @Data
    public static class AsientoVenta {
        private int fila;
        private int columna;
        private String persona;
    }
}
