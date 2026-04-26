package com.erts.repository;

import com.erts.model.Registration;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends MongoRepository<Registration, String> {
    List<Registration> findByEventId(String eventId);
    List<Registration> findByUserId(String userId);
    List<Registration> findByStatus(String status);
    List<Registration> findByEventIdAndStatus(String eventId, String status);
    Optional<Registration> findByReferenceNumber(String referenceNumber);
    long countByEventIdAndStatus(String eventId, String status);
}
