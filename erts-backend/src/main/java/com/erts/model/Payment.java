package com.erts.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String registrationId; // references registrations._id
    private String eventId;        // denormalised for easy admin queries
    private String userId;

    private String transactionId;  // UPI transaction ID submitted by user
    private String screenshotUrl;  // optional uploaded screenshot path

    private double amount;

    // pending | approved | rejected
    private String status;

    private String rejectionReason;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;     // admin userId who approved/rejected
}
