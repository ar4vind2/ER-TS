package com.erts.repository;

import com.erts.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByStatus(String status);
    List<Event> findByStatusIn(List<String> statuses);
    List<Event> findByStatusInAndCategory(List<String> statuses, String category);
    List<Event> findByOrganiserId(String organiserId);
    List<Event> findByTitleContainingIgnoreCase(String keyword);
}
