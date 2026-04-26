package com.erts.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "registrations")
public class Registration {

    @Id
    private String id;

    private String eventId;         // references events._id
    private String userId;          // references users._id (null if guest)

    private String type;            // solo | group

    // Primary attendee (the person filling the form)
    private Attendee primaryAttendee;

    // Additional members for group registration (empty list for solo)
    private List<Attendee> members;

    private String referenceNumber; // e.g. EVH-2025-7841
    private String status;          // pending | confirmed | rejected

    private double totalAmount;

    private LocalDateTime createdAt;

    // ── Nested class for attendee details ────────────────────────────────────
    @Data
    public static class Attendee {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String organisation;
        private String requirements;
    }
}
