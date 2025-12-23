package ar.edu.um.events_backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class Block_seat_db_Request {
    private Long eventoId;
    private List<AsientoRequest> asientos;

    @Data
    public static class AsientoRequest {
        private int fila;
        private int columna;
    }
}
