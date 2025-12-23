package ar.edu.um.events_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.events_backend.dto.Block_seat_db_Request;
import ar.edu.um.events_backend.dto.Block_seat_db_Response;
import ar.edu.um.events_backend.service.Block_seat_db_Service;

@RestController
@RequestMapping("/api/db/block-seats")
public class Block_seat_db_Controller {
    private final Block_seat_db_Service blockSeatService;

    public Block_seat_db_Controller(Block_seat_db_Service blockSeatService) {
        this.blockSeatService = blockSeatService;
    }

    @PostMapping
    public ResponseEntity<Block_seat_db_Response> blockSeats(@RequestBody Block_seat_db_Request request) {
        Block_seat_db_Response response = blockSeatService.blockSeatsExternal(request);
        return ResponseEntity.ok(response);
    }
}
