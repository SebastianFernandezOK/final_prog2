package ar.edu.um.events_backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class Block_seat_db_Response {
    private boolean resultado;
    private String descripcion;
    private Long eventoId;
    private List<AsientoEstado> asientos;

    @Data
    public static class AsientoEstado {
        private String estado;
        private int fila;
        private int columna;
    }
}
