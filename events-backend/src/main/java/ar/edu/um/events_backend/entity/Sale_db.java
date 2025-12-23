package ar.edu.um.events_backend.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale_db")
public class Sale_db {
    @Id
    private Long ventaId;

    private Long eventoId;
    private ZonedDateTime fechaVenta;
    private boolean resultado;
    
    @Column(length = 1000)
    private String descripcion;
    
    private Double precioVenta;
    private Integer cantidadAsientos;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String asientosJson;

    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }

    public ZonedDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(ZonedDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public boolean isResultado() { return resultado; }
    public void setResultado(boolean resultado) { this.resultado = resultado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }

    public Integer getCantidadAsientos() { return cantidadAsientos; }
    public void setCantidadAsientos(Integer cantidadAsientos) { this.cantidadAsientos = cantidadAsientos; }

    public String getAsientosJson() { return asientosJson; }
    public void setAsientosJson(String asientosJson) { this.asientosJson = asientosJson; }
}
