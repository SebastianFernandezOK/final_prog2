package ar.edu.um.events_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.events_backend.entity.EventSummary_db;
import ar.edu.um.events_backend.service.EventSummary_db_Service;

@RestController
@RequestMapping("/api/db/events/summary")
public class EventSummary_db_Controller {

    private final EventSummary_db_Service eventSummary_db_Service;

    public EventSummary_db_Controller(EventSummary_db_Service eventSummary_db_Service) {
        this.eventSummary_db_Service = eventSummary_db_Service;
    }

    @GetMapping
    public ResponseEntity<List<EventSummary_db>> getAllEvents() {
        List<EventSummary_db> events = eventSummary_db_Service.findAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventSummary_db> getEventById(@PathVariable Long id) {
        return eventSummary_db_Service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
