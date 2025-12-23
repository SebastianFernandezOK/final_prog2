package ar.edu.um.events_proxy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeatDTO {
    
    @JsonProperty("fila")
    private Integer fila;
    
    @JsonProperty("columna")
    private Integer columna;
    
    @JsonProperty("estado")
    private String estado;
    
    @JsonProperty("expira")
    private String expira;

    public SeatDTO() {
    }

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public Integer getColumna() {
        return columna;
    }

    public void setColumna(Integer columna) {
        this.columna = columna;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getExpira() {
        return expira;
    }

    public void setExpira(String expira) {
        this.expira = expira;
    }
}
