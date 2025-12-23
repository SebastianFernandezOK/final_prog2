package ar.edu.um.events_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.um.events_backend.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByNameOrMail(String name, String mail);
}
