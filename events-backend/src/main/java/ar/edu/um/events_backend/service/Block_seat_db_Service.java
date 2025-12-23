package ar.edu.um.events_backend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ar.edu.um.events_backend.dto.Block_seat_db_Request;
import ar.edu.um.events_backend.dto.Block_seat_db_Response;

@Service
public class Block_seat_db_Service {
    private final WebClient externalApiClient;

    public Block_seat_db_Service(@Qualifier("externalApiClient") WebClient externalApiClient) {
        this.externalApiClient = externalApiClient;
    }

    public Block_seat_db_Response blockSeatsExternal(Block_seat_db_Request request) {
        return externalApiClient
            .post()
            .uri("/api/endpoints/v1/bloquear-asientos")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Block_seat_db_Response.class)
            .block();
    }
}
