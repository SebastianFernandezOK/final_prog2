package ar.edu.um.events_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.events_backend.entity.Sale_db;

@Repository
public interface Sale_db_Repository extends JpaRepository<Sale_db, Long> {
    List<Sale_db> findByEventoId(Long eventoId);
}
