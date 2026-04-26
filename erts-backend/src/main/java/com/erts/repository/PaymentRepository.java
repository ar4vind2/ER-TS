package com.erts.repository;

import com.erts.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByStatus(String status);
    List<Payment> findByEventId(String eventId);
    Optional<Payment> findByRegistrationId(String registrationId);
    List<Payment> findByUserId(String userId);
}
