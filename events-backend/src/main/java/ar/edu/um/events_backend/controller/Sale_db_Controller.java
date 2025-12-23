package ar.edu.um.events_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.events_backend.entity.Sale_db;
import ar.edu.um.events_backend.service.Sale_db_Service;

@RestController
@RequestMapping("/api/db/sales")
public class Sale_db_Controller {
    
    private final Sale_db_Service saleService;
    
    public Sale_db_Controller(Sale_db_Service saleService) {
        this.saleService = saleService;
    }
    
    @GetMapping
    public ResponseEntity<List<Sale_db>> getAllSales() {
        return ResponseEntity.ok(saleService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Sale_db> getSaleById(@PathVariable Long id) {
        return saleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/event/{eventoId}")
    public ResponseEntity<List<Sale_db>> getSalesByEventoId(@PathVariable Long eventoId) {
        return ResponseEntity.ok(saleService.findByEventoId(eventoId));
    }
}
