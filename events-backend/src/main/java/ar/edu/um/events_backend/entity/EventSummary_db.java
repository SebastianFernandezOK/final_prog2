package ar.edu.um.events_backend.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_summary_db")
public class EventSummary_db {
    @Id
    private Long id;

    private String titulo;
    private String resumen;
    private String descripcion;
    private ZonedDateTime fecha;
    private BigDecimal precioEntrada;
    private String eventoTipoNombre;
    private String eventoTipoDescripcion;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public ZonedDateTime getFecha() { return fecha; }
    public void setFecha(ZonedDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getPrecioEntrada() { return precioEntrada; }
    public void setPrecioEntrada(BigDecimal precioEntrada) { this.precioEntrada = precioEntrada; }

    public String getEventoTipoNombre() { return eventoTipoNombre; }
    public void setEventoTipoNombre(String eventoTipoNombre) { this.eventoTipoNombre = eventoTipoNombre; }

    public String getEventoTipoDescripcion() { return eventoTipoDescripcion; }
    public void setEventoTipoDescripcion(String eventoTipoDescripcion) { this.eventoTipoDescripcion = eventoTipoDescripcion; }
}
