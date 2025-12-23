package ar.edu.um.events_backend.dto;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class Sale_summary_DTO {
    private Long eventoId;
    private Long ventaId;
    private ZonedDateTime fechaVenta;
    private boolean resultado;
    private String descripcion;
    private Double precioVenta;
    private Integer cantidadAsientos;
}
