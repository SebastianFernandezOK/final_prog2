package ar.edu.um.events_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.events_backend.entity.Event_db;
import ar.edu.um.events_backend.service.Event_db_Service;

@RestController
@RequestMapping("/api/db/events")
public class Events_db_Controller {

    private final Event_db_Service eventDbService;

    public Events_db_Controller(Event_db_Service eventDbService) {
        this.eventDbService = eventDbService;
    }

    @GetMapping
    public ResponseEntity<List<Event_db>> getAllEvents() {
        List<Event_db> events = eventDbService.findAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event_db> getEventById(@PathVariable Long id) {
        return eventDbService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
