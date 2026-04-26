package com.erts.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "tickets")
public class Ticket {

    @Id
    private String id;

    private String registrationId; // references registrations._id
    private String paymentId;      // references payments._id
    private String eventId;
    private String userId;

    private String referenceNumber; // same as registration ref, e.g. EVH-2025-7841
    private String qrCodeData;      // value encoded in the QR (typically referenceNumber)

    // active | revoked
    private String status;

    private LocalDateTime issuedAt;
    private LocalDateTime revokedAt;
    private String revokedBy;
}
