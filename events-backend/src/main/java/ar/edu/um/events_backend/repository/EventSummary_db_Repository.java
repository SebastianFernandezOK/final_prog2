package ar.edu.um.events_backend.repository;

import ar.edu.um.events_backend.entity.EventSummary_db;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventSummary_db_Repository extends JpaRepository<EventSummary_db, Long> {
    // Custom queries can be added here if needed
}
