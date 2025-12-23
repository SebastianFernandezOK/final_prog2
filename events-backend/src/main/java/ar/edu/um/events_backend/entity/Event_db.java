package ar.edu.um.events_backend.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_db")
public class Event_db {
    @Id
    private Long id;
    
    private String titulo;
    private String resumen;
    
    @Column(length = 1000)
    private String descripcion;
    
    private ZonedDateTime fecha;
    private String direccion;
    
    @Column(length = 1000)
    private String imagen;
    
    private Integer filaAsientos;
    private Integer columnAsientos;
    private BigDecimal precioEntrada;
    
    private String eventoTipoNombre;
    private String eventoTipoDescripcion;
    
    @ElementCollection
    @CollectionTable(name = "event_integrantes", joinColumns = @JoinColumn(name = "event_id"))
    private List<Integrante> integrantes = new ArrayList<>();
    
    @Embeddable
    public static class Integrante {
        private String nombre;
        private String apellido;
        private String identificacion;
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        
        public String getIdentificacion() { return identificacion; }
        public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    }
    
    // Getters y setters
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
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    
    public Integer getFilaAsientos() { return filaAsientos; }
    public void setFilaAsientos(Integer filaAsientos) { this.filaAsientos = filaAsientos; }
    
    public Integer getColumnAsientos() { return columnAsientos; }
    public void setColumnAsientos(Integer columnAsientos) { this.columnAsientos = columnAsientos; }
    
    public BigDecimal getPrecioEntrada() { return precioEntrada; }
    public void setPrecioEntrada(BigDecimal precioEntrada) { this.precioEntrada = precioEntrada; }
    
    public String getEventoTipoNombre() { return eventoTipoNombre; }
    public void setEventoTipoNombre(String eventoTipoNombre) { this.eventoTipoNombre = eventoTipoNombre; }
    
    public String getEventoTipoDescripcion() { return eventoTipoDescripcion; }
    public void setEventoTipoDescripcion(String eventoTipoDescripcion) { this.eventoTipoDescripcion = eventoTipoDescripcion; }
    
    public List<Integrante> getIntegrantes() { return integrantes; }
    public void setIntegrantes(List<Integrante> integrantes) { this.integrantes = integrantes; }
}
