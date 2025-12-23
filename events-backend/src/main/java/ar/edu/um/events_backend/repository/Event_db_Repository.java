package ar.edu.um.events_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.um.events_backend.entity.Event_db;

@Repository
public interface Event_db_Repository extends JpaRepository<Event_db, Long> {
}
