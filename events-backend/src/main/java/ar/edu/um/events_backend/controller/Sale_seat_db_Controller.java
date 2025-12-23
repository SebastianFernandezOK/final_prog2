package ar.edu.um.events_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.events_backend.dto.Sale_seat_db_Request;
import ar.edu.um.events_backend.dto.Sale_seat_db_Response;
import ar.edu.um.events_backend.service.Sale_seat_db_Service;

@RestController
@RequestMapping("/api/db/sale-seats")
public class Sale_seat_db_Controller {

    private final Sale_seat_db_Service saleSeatsService;

    public Sale_seat_db_Controller(Sale_seat_db_Service saleSeatsService) {
        this.saleSeatsService = saleSeatsService;
    }

    @PostMapping
    public ResponseEntity<Sale_seat_db_Response> sellSeats(@RequestBody Sale_seat_db_Request request) {
        Sale_seat_db_Response response = saleSeatsService.sellSeats(request);
        return ResponseEntity.ok(response);
    }
}
