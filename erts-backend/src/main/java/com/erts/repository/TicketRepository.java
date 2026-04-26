package com.erts.repository;

import com.erts.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByUserId(String userId);
    List<Ticket> findByEventId(String eventId);
    Optional<Ticket> findByRegistrationId(String registrationId);
    Optional<Ticket> findByReferenceNumber(String referenceNumber);
    List<Ticket> findByStatus(String status);
}
