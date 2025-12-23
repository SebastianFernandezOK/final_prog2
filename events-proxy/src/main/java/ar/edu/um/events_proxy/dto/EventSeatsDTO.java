package ar.edu.um.events_proxy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EventSeatsDTO {
    
    @JsonProperty("eventoId")
    private Integer eventoId;
    
    @JsonProperty("asientos")
    private List<SeatDTO> asientos;

    public EventSeatsDTO() {
    }

    public Integer getEventoId() {
        return eventoId;
    }

    public void setEventoId(Integer eventoId) {
        this.eventoId = eventoId;
    }

    public List<SeatDTO> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<SeatDTO> asientos) {
        this.asientos = asientos;
    }
}
