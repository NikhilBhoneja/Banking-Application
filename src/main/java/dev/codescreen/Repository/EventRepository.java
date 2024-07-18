package dev.codescreen.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.codescreen.model.Event;

public interface EventRepository extends JpaRepository<Event, String> {

}
