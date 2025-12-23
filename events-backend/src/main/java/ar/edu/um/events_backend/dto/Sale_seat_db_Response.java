package ar.edu.um.events_backend.dto;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Sale_seat_db_Response {
    private Long eventoId;
    private Long ventaId;
    private ZonedDateTime fechaVenta;
    private List<AsientoVendido> asientos;
    private boolean resultado;
    private String descripcion;
    private Double precioVenta;

    @Data
    public static class AsientoVendido {
        private int fila;
        private int columna;
        private String persona;
        private String estado;
    }
}
