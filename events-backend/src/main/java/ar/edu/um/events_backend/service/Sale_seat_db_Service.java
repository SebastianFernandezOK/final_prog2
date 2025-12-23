package ar.edu.um.events_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ar.edu.um.events_backend.dto.Sale_seat_db_Request;
import ar.edu.um.events_backend.dto.Sale_seat_db_Response;
import reactor.core.publisher.Mono;

@Service
public class Sale_seat_db_Service {

    private static final Logger logger = LoggerFactory.getLogger(Sale_seat_db_Service.class);

    private final WebClient externalApiClient;
    private final Sale_db_Service saleDbService;

    public Sale_seat_db_Service(@Qualifier("externalApiClient") WebClient externalApiClient,
                                 Sale_db_Service saleDbService) {
        this.externalApiClient = externalApiClient;
        this.saleDbService = saleDbService;
    }

    public Sale_seat_db_Response sellSeats(Sale_seat_db_Request request) {
        try {
            Sale_seat_db_Response response = externalApiClient.post()
                    .uri("/api/endpoints/v1/realizar-venta")
                    .body(Mono.just(request), Sale_seat_db_Request.class)
                    .retrieve()
                    .bodyToMono(Sale_seat_db_Response.class)
                    .block();
            
            if (response != null && response.getVentaId() != null) {
                try {
                    logger.info("Venta realizada: ventaId={}, eventoId={}, resultado={}", 
                               response.getVentaId(), response.getEventoId(), response.isResultado());
                    
                    saleDbService.syncSalesFromExternal();
                    
                } catch (Exception e) {
                    logger.error("Error al sincronizar ventas desde cátedra: {}", e.getMessage(), e);
                }
            }
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al realizar la venta de asientos: " + e.getMessage(), e);
        }
    }
}
